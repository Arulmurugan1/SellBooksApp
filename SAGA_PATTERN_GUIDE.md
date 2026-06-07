# Saga Pattern Implementation Guide

## Overview

The Saga Pattern is a design pattern for managing distributed transactions across microservices. Instead of using traditional ACID transactions that lock data, the Saga Pattern breaks down a business transaction into a series of local transactions executed by individual services.

## Key Concepts

### 1. **Eventual Consistency**
- Services eventually reach a consistent state
- No global locking or immediate rollbacks
- Compensation transactions handle failures

### 2. **Loose Coupling**
- Services communicate through events
- No direct synchronous calls for saga orchestration
- Event-driven asynchronous communication

### 3. **Compensation**
- Each saga step has a corresponding compensation
- If a step fails, previous steps are "undone"
- Maintains data consistency across services

## Two Approaches

### A. Choreography (Event-Driven)
- Services listen to events and trigger actions
- No central orchestrator
- **Used in this project**

### B. Orchestration (Command-Driven)
- Central orchestrator service coordinates steps
- Explicit workflow definition
- Easier to debug but more coupling

## Book Selling Application - Saga Flow

```
┌──────────────────────────────────────────────────────────┐
│                  Order Creation Saga                      │
└──────────────────────────────────────────────────────────┘

Step 1: Customer Submits Order
├─ Input: Items, Quantity, Billing Info
├─ Service: Order Service
├─ Action: Create order with PENDING status
└─ Output: Order ID

Step 2: Check Product Availability (Synchronous)
├─ Input: Product IDs
├─ Service: Order Service → Product Service
├─ Action: Verify products are available
├─ Retry: 3 attempts with 1-second wait
├─ Circuit Breaker: Open after 50% failure rate
└─ Output: Availability status or exception

Step 3: Reserve Inventory (Synchronous)
├─ Input: Order ID, Product IDs, Quantities
├─ Service: Order Service → Inventory Service
├─ Action: Reserve stock and create reservation records
├─ Update: Move stock from "available" to "reserved"
├─ Rollback: Manual stock release on failure
└─ Output: Reservation confirmation

Step 4: Publish Order Created Event (Asynchronous)
├─ Input: Order details
├─ Service: Order Service → Kafka
├─ Action: Publish to "order-created" topic
├─ Message: OrderCreatedEvent (JSON)
└─ Subscribers: Payment Service

Step 5: Process Payment (Asynchronous)
├─ Input: OrderCreatedEvent
├─ Service: Payment Service (Kafka consumer)
├─ Action: Create payment record, process payment
├─ Timeout: 2000ms per payment processor
├─ Simulation: 70% success, 30% failure
└─ Output: PaymentProcessedEvent

Step 6A: Payment Success Path
├─ Event: PaymentProcessedEvent (SUCCESS)
├─ Service: Order Service (Kafka consumer)
├─ Action: Update order status to CONFIRMED
├─ Email: Send confirmation email (async)
└─ Result: Order complete, customer receives confirmation

Step 6B: Payment Failure Path (Compensation)
├─ Event: PaymentProcessedEvent (FAILED)
├─ Service: Order Service (Kafka consumer)
├─ Action 1: Update order status to PAYMENT_FAILED
├─ Action 2: Release reserved inventory
│   └─ Inventory Service: Move stock back to available
├─ Action 3: Publish OrderCancelledEvent
├─ Email: Send failure notification (async)
└─ Result: Order cancelled, inventory restored, eventual consistency achieved
```

## Implementation Details

### Database Transitions

**Order Status Lifecycle**:
```
PENDING 
  ↓
INVENTORY_RESERVED (after inventory check succeeds)
  ↓
PAYMENT_PROCESSING (after payment starts)
  ├─→ CONFIRMED (payment success)
  │
  └─→ PAYMENT_FAILED (payment failure)
      └─→ CANCELLED (compensation complete)
```

**Inventory Reservation Lifecycle**:
```
RESERVED (initial reservation)
  ├─→ CONFIRMED (order confirmed)
  │
  └─→ RELEASED (order cancelled/compensation)
```

**Payment Status Lifecycle**:
```
PENDING → PROCESSING → SUCCESS/FAILED
```

### Kafka Topics & Messages

**Topic: order-created**
```json
{
  "orderId": "ORD-001",
  "customerId": "CUST-001",
  "customerEmail": "customer@example.com",
  "items": [
    {
      "productId": "PROD001",
      "quantity": 2,
      "price": 10.99
    }
  ],
  "totalAmount": 21.98,
  "status": "PENDING",
  "timestamp": 1686038400000
}
```

**Topic: payment-processed**
```json
{
  "orderId": "ORD-001",
  "transactionId": "TXN-001",
  "amount": 21.98,
  "status": "SUCCESS|FAILED",
  "message": "Payment processed successfully or reason for failure",
  "timestamp": 1686038401000
}
```

## Failure Scenarios & Recovery

### Scenario 1: Product Not Available
```
┌─ Order Service creates order (PENDING)
├─ Product Service check fails (product not available)
├─ Order Service throws exception
└─ No event published, no compensation needed
   (Order remains PENDING, can be retried or cancelled manually)
```

### Scenario 2: Inventory Insufficient
```
┌─ Order Service creates order (PENDING)
├─ Product Service check succeeds
├─ Inventory Service fails (insufficient stock)
├─ Order Service throws exception
└─ No event published, inventory not changed
   (Order remains PENDING, can be retried)
```

### Scenario 3: Payment Fails (Most Important Saga Compensation)
```
┌─ Order Service creates order (PENDING)
├─ Product Service check succeeds
├─ Inventory Service reserves stock (INVENTORY_RESERVED)
├─ OrderCreatedEvent published
├─ Payment Service processes payment → FAILS
├─ PaymentProcessedEvent (FAILED) published
│
├─ Order Service receives event
│  ├─ Update order status → PAYMENT_FAILED
│  └─ Invoke compensation
│
├─ Inventory Service releases reserved stock
│  └─ Move stock back to "available"
│
├─ OrderCancelledEvent published
│
└─ Email Service sends failure notification
   (System is eventually consistent)
```

### Scenario 4: Inventory Release Fails
```
┌─ Payment fails
├─ Inventory release fails (network timeout)
├─ Order Service logs error and retries
├─ Manual intervention may be needed (DLQ processing)
│
├─ Monitoring Alert: Check for unreleased inventory
├─ Admin Action: Release manually via API
└─ Trigger compensation endpoint for order
```

### Scenario 5: Idempotency (Duplicate Events)
```
┌─ Payment Service publishes PaymentProcessedEvent
├─ Message published twice due to network issue
├─ Order Service receives first event
│  └─ Updates order status → CONFIRMED
│
├─ Order Service receives duplicate event
│  ├─ Check: Order already CONFIRMED?
│  └─ Action: Ignore (idempotent operation)
│
└─ Result: No double-processing, eventual consistency maintained
```

## Resilience Patterns Applied

### 1. **Circuit Breaker** (Synchronous Calls)
```java
@CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
public OrderDTO createOrder(OrderDTO orderDTO) {
    // Order creation logic
}

public OrderDTO createOrderFallback(OrderDTO orderDTO, Exception ex) {
    throw new RuntimeException("Service temporarily unavailable");
}
```

**Behavior**:
- **Closed**: Requests flow normally
- **Open**: Requests fail immediately (after 50% failure rate)
- **Half-Open**: Test requests to determine if service recovered

### 2. **Retry** (Transient Failures)
```yaml
resilience4j:
  retry:
    instances:
      orderService:
        maxAttempts: 3           # 3 total attempts
        waitDuration: 1000       # 1 second between retries
        retryExceptions:
          - java.io.IOException
          - feign.FeignException
```

**Example**: Product Service call fails → Retry 2 more times → Fail if all 3 fail

### 3. **Timeout** (Prevent Hanging Requests)
- Default timeout: 2000ms
- Prevents cascading failures
- Triggers fallback response if exceeded

### 4. **Fallback** (Graceful Degradation)
- When circuit breaker opens
- Returns default response
- Prevents entire system failure

## Code Examples

### Order Service - Publishing Event
```java
private void publishOrderCreatedEvent(Order order) {
    OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .customerEmail(order.getCustomerEmail())
            .items(order.getItems().stream()...build())
            .totalAmount(order.getTotalAmount())
            .timestamp(System.currentTimeMillis())
            .build();
    
    orderEventProducer.publishOrderCreatedEvent(event);
    log.info("Order created event published for orderId: {}", order.getOrderId());
}
```

### Payment Service - Consuming Event
```java
@Bean
public Consumer<OrderCreatedEvent> orderCreated() {
    return event -> {
        log.info("Received OrderCreatedEvent for orderId: {}", event.getOrderId());
        try {
            paymentService.processPayment(
                    event.getOrderId(),
                    event.getCustomerId(),
                    event.getTotalAmount()
            );
        } catch (Exception ex) {
            paymentService.publishPaymentFailedEvent(
                    event.getOrderId(),
                    "Payment processing error: " + ex.getMessage()
            );
        }
    };
}
```

### Order Service - Compensation (Inventory Release)
```java
public void handlePaymentFailure(String orderId, String reason) {
    log.info("Handling payment failure for orderId: {}", orderId);
    
    Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    
    // Release reserved inventory
    List<ReleaseItem> releaseItems = order.getItems().stream()
            .map(item -> new ReleaseItem(item.getProductId(), item.getQuantity()))
            .collect(Collectors.toList());
    
    ReleaseStockRequest releaseRequest = 
            new ReleaseStockRequest(orderId, releaseItems);
    
    inventoryServiceClient.releaseStock(releaseRequest);
    
    // Update order status
    order.setStatus(Order.OrderStatus.PAYMENT_FAILED);
    orderRepository.save(order);
    
    // Publish cancellation event
    orderEventProducer.publishOrderCancelledEvent(orderId, reason);
}
```

## Monitoring & Debugging

### Key Metrics to Monitor
```
1. Order Success Rate: (Confirmed Orders / Total Orders) × 100%
2. Payment Success Rate: (Successful Payments / Total Payments) × 100%
3. Avg Order Processing Time: Total Time / Number of Orders
4. Saga Compensation Rate: (Compensations / Total Orders) × 100%
5. Circuit Breaker State: CLOSED / OPEN / HALF_OPEN
6. Kafka Consumer Lag: Messages waiting to be processed
```

### Log Analysis
```bash
# Check for payment failures
grep "Payment failed" payment-service/logs/*.log

# Check for compensation triggers
grep "handlePaymentFailure" order-service/logs/*.log

# Check for inventory releases
grep "Stock released" inventory-service/logs/*.log
```

### Tracing Order Flow
```
1. Order created: ORDER-001
   └─ Find in order_db: SELECT * FROM orders WHERE order_id = 'ORDER-001'

2. Payment processed:
   └─ Find in payment_db: SELECT * FROM payments WHERE order_id = 'ORDER-001'

3. Inventory reserved:
   └─ Find in inventory_db: SELECT * FROM inventory_reservations 
      WHERE order_id = 'ORDER-001'

4. Check order status flow:
   └─ SELECT status FROM orders WHERE order_id = 'ORDER-001'
      Verify: PENDING → INVENTORY_RESERVED → CONFIRMED/PAYMENT_FAILED
```

## Best Practices

### ✅ DO
- Use idempotent operations in event consumers
- Log saga steps for debugging
- Set appropriate timeouts
- Implement compensation for each saga step
- Monitor circuit breaker states
- Track saga completion times
- Use unique order IDs
- Publish events after database changes

### ❌ DON'T
- Use saga pattern for simple transactions (use DB transactions)
- Skip compensation logic
- Hard-code timeouts
- Ignore circuit breaker states
- Process events without idempotency checks
- Mix choreography and orchestration
- Assume events are delivered exactly once
- Skip error logging and monitoring

## Testing Strategy

### Unit Tests
```
- OrderService.createOrder() with mocks
- PaymentService.processPayment() logic
- InventoryService.reserveStock() logic
```

### Integration Tests
```
- Create order → Verify inventory reserved
- Process payment → Verify order updated
- Payment fails → Verify inventory released
- Kafka event flow
```

### End-to-End Tests
```
- Full order creation flow
- Payment success/failure paths
- Compensation triggers
- Concurrent order processing
```

### Load Tests
```
- 100 concurrent orders
- Payment processing under load
- Kafka throughput
- Database connection pool
```

## Conclusion

The Saga Pattern used in this application provides:
1. **Resilience**: Handles partial failures gracefully
2. **Scalability**: Services scale independently
3. **Flexibility**: Easy to add new steps or services
4. **Maintainability**: Clear separation of concerns
5. **Consistency**: Eventual consistency through compensation

The combination of synchronous checks (product/inventory) and asynchronous processing (payments) ensures fast validation while handling long-running operations efficiently.
