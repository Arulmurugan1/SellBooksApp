package com.booksellingapp.payment.event;

import com.booksellingapp.common.event.OrderCreatedEvent;
import com.booksellingapp.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;

    /**
     * Consumer function for order created events
     */
    @Bean
    public Consumer<OrderCreatedEvent> orderCreated() {
        return event -> {
            log.info("Received OrderCreatedEvent for orderId: {}", event.getOrderId());
            
            try {
                // Process payment asynchronously
                paymentService.processPayment(
                        event.getOrderId(),
                        event.getCustomerId(),
                        event.getTotalAmount()
                );
            } catch (Exception ex) {
                log.error("Error processing payment for orderId: {}", event.getOrderId(), ex);
                // Publish payment failed event
                paymentService.publishPaymentFailedEvent(
                        event.getOrderId(),
                        "Payment processing error: " + ex.getMessage()
                );
            }
        };
    }
}
