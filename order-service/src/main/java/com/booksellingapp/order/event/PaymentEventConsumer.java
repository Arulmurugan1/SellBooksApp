package com.booksellingapp.order.event;

import com.booksellingapp.common.event.PaymentProcessedEvent;
import com.booksellingapp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    /**
     * Consumer function for payment processed events
     */
    @Bean
    public Consumer<PaymentProcessedEvent> paymentProcessed() {
        return event -> {
            log.info("Received PaymentProcessedEvent for orderId: {}", event.getOrderId());

            if ("SUCCESS".equals(event.getStatus())) {
                log.info("Payment successful for orderId: {}", event.getOrderId());
                orderService.handlePaymentSuccess(event.getOrderId());
                // Send email notification asynchronously
                sendOrderConfirmationEmail(event.getOrderId());
            } else if ("FAILED".equals(event.getStatus())) {
                log.warn("Payment failed for orderId: {}", event.getOrderId());
                orderService.handlePaymentFailure(event.getOrderId(), event.getMessage());
                // Send payment failure email asynchronously
                sendPaymentFailureEmail(event.getOrderId());
            }
        };
    }

    /**
     * Send order confirmation email asynchronously
     */
    private void sendOrderConfirmationEmail(String orderId) {
        // This would typically be sent to a message queue
        // for async processing by an email service
        log.info("Queuing order confirmation email for orderId: {}", orderId);
    }

    /**
     * Send payment failure email asynchronously
     */
    private void sendPaymentFailureEmail(String orderId) {
        // This would typically be sent to a message queue
        // for async processing by an email service
        log.info("Queuing payment failure email for orderId: {}", orderId);
    }
}
