package com.booksellingapp.order.service;

import com.booksellingapp.common.event.OrderCreatedEvent;
import com.booksellingapp.order.client.InventoryServiceClient;
import com.booksellingapp.order.client.ProductServiceClient;
import com.booksellingapp.order.dto.OrderDTO;
import com.booksellingapp.order.entity.Order;
import com.booksellingapp.order.entity.OrderItem;
import com.booksellingapp.order.event.OrderEventProducer;
import com.booksellingapp.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final OrderEventProducer orderEventProducer;

    /**
     * Create a new order using Saga Pattern
     * 1. Create order with PENDING status
     * 2. Check product availability (Synchronous)
     * 3. Check inventory (Synchronous)
     * 4. Publish event for payment processing (Asynchronous)
     */
    @Transactional
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    @Retry(name = "orderService")
    public OrderDTO createOrder(OrderDTO orderDTO) {
        
        log.info("Creating order for customer: {}", orderDTO.getCustomerId());
        log.info("OrderDTO received: {}", orderDTO);

        // Step 1: Create order with PENDING status
        String orderId = UUID.randomUUID().toString();

        log.info("Generated orderId: {}", orderId);

        Order order = Order.builder()
                .orderId(orderId)
                .customerId(orderDTO.getCustomerId())
                .customerEmail(orderDTO.getCustomerEmail())
                .totalAmount(BigDecimal.ZERO)
                .status(Order.OrderStatus.PENDING)
                .build();


        log.info("Order entity created with orderId: {} - Status: {}", order.getOrderId(), order.getStatus());

        // Persist first to get generated DB id used by order_items.order_id
        order = orderRepository.save(order);
        log.info("Order persisted with id: {} and orderId: {}", order.getId(), order.getOrderId());
        
        // Step 2: Validate products and calculate total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        log.info("Validating products and calculating total for orderId: {}", orderId);

        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            // Synchronous call to Product Service
            ProductServiceClient.ProductResponse product = 
                productServiceClient.getProductByCode(itemDTO.getProductId());

            if (!product.available()) {
                log.error("Product not available: {}", itemDTO.getProductId());
                throw new RuntimeException("Product not available: " + itemDTO.getProductId());
            }

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(itemDTO.getProductId())
                    .productName(product.title())
                    .quantity(itemDTO.getQuantity())
                    .price(product.price())
                    .totalPrice(product.price().multiply(new BigDecimal(itemDTO.getQuantity())))
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            log.info("Order item added: {} - Quantity: {} - Total Price: {}",
                    orderItem.getProductId(), orderItem.getQuantity(), orderItem.getTotalPrice());
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        log.info("Total amount calculated for orderId: {} - Total Amount: {}", orderId, totalAmount);
        log.info("Order entity before inventory check: {}", order);

        log.info("Product validation and total calculation completed for orderId: {}", orderId);

        // Step 3: Check and reserve inventory (Synchronous)
        checkAndReserveInventory(order);

        log.info("Order entity after inventory reservation: {}", order);

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with orderId: {}", savedOrder.getOrderId());

        log.info("Saved Order entity: {}", savedOrder);

        // Step 4: Publish event for asynchronous payment processing
        publishOrderCreatedEvent(savedOrder);

        log.info("Order created event published for orderId: {}", savedOrder.getOrderId());

        return convertToDTO(savedOrder);
    }

    /**
     * Fallback method for circuit breaker
     */
    public OrderDTO createOrderFallback(OrderDTO orderDTO, Exception ex) {
        log.error("Circuit breaker opened for order creation. Error: {}", ex.getMessage());
        throw new RuntimeException("Order service temporarily unavailable. Please try again later.");
    }

    /**
     * Check inventory and reserve stock
     */
    private void checkAndReserveInventory(Order order) {
        log.info("Checking and reserving inventory for orderId: {}", order.getOrderId());

        // Prepare stock check request
        List<InventoryServiceClient.StockItem> stockItems = order.getItems().stream()
                .map(item -> new InventoryServiceClient.StockItem(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        log.info("Stock check request prepared for orderId: {} - Stock Items: {}", order.getOrderId(), stockItems);

        // Check stock availability
        InventoryServiceClient.StockCheckResponse checkResponse =
                inventoryServiceClient.checkStock(stockItems);

        log.info("Stock check response for orderId: {} - All Items Available: {}, Details: {}",
                order.getOrderId(), checkResponse.allItemsAvailable(), checkResponse.details());

        if (!checkResponse.allItemsAvailable()) {
            log.error("Insufficient inventory for orderId: {} - Details: {}", order.getOrderId(), checkResponse.details());
            throw new RuntimeException("Insufficient inventory for some items");
        }

        log.info("Stock available for all items in orderId: {}", order.getOrderId());

        // Reserve stock
        List<InventoryServiceClient.ReservationItem> reservationItems = order.getItems().stream()
                .map(item -> new InventoryServiceClient.ReservationItem(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        log.info("Reservation request prepared for orderId: {} - Reservation Items: {}", order.getOrderId(), reservationItems);

        inventoryServiceClient.reserveStock(order.getOrderId(), reservationItems);
        // Update order status
        order.setStatus(Order.OrderStatus.INVENTORY_RESERVED);
        log.info("Inventory reserved for orderId: {}", order.getOrderId());
    }

    /**
     * Publish order created event for payment processing (Asynchronous)
     */
    private void publishOrderCreatedEvent(Order order) {
        log.info("Publishing order created event for orderId: {}", order.getOrderId());
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .items(order.getItems().stream()
                        .map(item -> OrderCreatedEvent.OrderItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .timestamp(System.currentTimeMillis())
                .build();

        orderEventProducer.publishOrderCreatedEvent(event);
        log.info("Order created event published for orderId: {}", order.getOrderId());
    }

    /**
     * Handle payment success event - update order status to CONFIRMED
     */
    @Transactional
    public void handlePaymentSuccess(String orderId) {
        log.info("Handling payment success for orderId: {}", orderId);

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Order confirmed with orderId: {}", orderId);
    }

    /**
     * Handle payment failure event - cancel order and release inventory
     */
    @Transactional
    public void handlePaymentFailure(String orderId, String reason) {
        log.info("Handling payment failure for orderId: {} - Reason: {}", orderId, reason);

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Release reserved inventory
        List<InventoryServiceClient.ReleaseItem> releaseItems = order.getItems().stream()
                .map(item -> new InventoryServiceClient.ReleaseItem(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        try {
            inventoryServiceClient.releaseStock(orderId, releaseItems);
        } catch (Exception ex) {
            log.error("Failed to release inventory for orderId: {}", orderId, ex);
        }

        order.setStatus(Order.OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        // Publish order cancelled event
        orderEventProducer.publishOrderCancelledEvent(orderId, reason);

        log.info("Order cancelled with orderId: {}", orderId);
    }

    /**
     * Get order by orderId
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderId(String orderId) {
        log.info("Fetching order with orderId: {}", orderId);
        return orderRepository.findByOrderId(orderId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    /**
     * Get all orders by customerId
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomerId(String customerId) {
        log.info("Fetching orders for customerId: {}", customerId);
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Order entity to OrderDTO
     */
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .items(order.getItems().stream()
                        .map(item -> OrderDTO.OrderItemDTO.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .totalPrice(item.getTotalPrice())
                                .build())
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .build();
    }
}
