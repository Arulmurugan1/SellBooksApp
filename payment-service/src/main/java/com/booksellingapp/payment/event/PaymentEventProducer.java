package com.booksellingapp.payment.event;

import com.booksellingapp.common.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final StreamBridge streamBridge;

    /**
     * Publish payment processed event
     */
    public void publishPaymentProcessedEvent(String orderId, String transactionId, BigDecimal amount, 
                                             String status, String message) {
        log.info("Publishing PaymentProcessedEvent for orderId: {}, status: {}", orderId, status);
        
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .amount(amount)
                .status(status)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
        
        streamBridge.send("paymentProcessed-out-0", event);
    }
}
