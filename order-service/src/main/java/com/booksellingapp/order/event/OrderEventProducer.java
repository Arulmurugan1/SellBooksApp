package com.booksellingapp.order.event;

import com.booksellingapp.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final StreamBridge streamBridge;

    /**
     * Publish order created event
     */
    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId: {}", event.getOrderId());
        streamBridge.send("orderCreated-out-0", event);
    }

    /**
     * Publish order cancelled event
     */
    public void publishOrderCancelledEvent(String orderId, String reason) {
        log.info("Publishing OrderCancelledEvent for orderId: {}", orderId);
        streamBridge.send("orderCancelled-out-0", 
            new OrderCancelledEvent(orderId, reason, System.currentTimeMillis()));
        log.info("OrderCancelledEvent published for orderId: {}", orderId);
    }

    public record OrderCancelledEvent(
            String orderId,
            String reason,
            Long timestamp
    ) {}
}
