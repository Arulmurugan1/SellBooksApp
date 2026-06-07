package com.booksellingapp.payment.service;

import com.booksellingapp.payment.dto.PaymentDTO;
import com.booksellingapp.payment.entity.Payment;
import com.booksellingapp.payment.event.PaymentEventProducer;
import com.booksellingapp.payment.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final Random random = new Random();

    /**
     * Process payment for an order
     * This is called asynchronously when an order is created
     * Uses Saga Pattern - publishes event after processing
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentService")
    public void processPayment(String orderId, String customerId, BigDecimal amount) {
        log.info("Processing payment for orderId: {}, amount: {}", orderId, amount);

        String transactionId = generateTransactionId();
        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .paymentMethod(Payment.PaymentMethod.CARD)
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        paymentRepository.save(payment);
        log.debug("Payment record created with transactionId: {}", transactionId);

        // Simulate payment processing with random success/failure
        boolean paymentSuccess = simulatePaymentProcessing();

        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            log.info("Payment successful for orderId: {}", orderId);

            // Publish payment success event
            paymentEventProducer.publishPaymentProcessedEvent(
                    orderId,
                    transactionId,
                    amount,
                    "SUCCESS",
                    "Payment processed successfully"
            );
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds or card declined");
            paymentRepository.save(payment);
            log.error("Payment failed for orderId: {}", orderId);

            // Publish payment failed event - triggers compensation (order cancellation)
            publishPaymentFailedEvent(orderId, payment.getFailureReason());
        }
    }

    /**
     * Fallback method for circuit breaker
     */
    public void processPaymentFallback(String orderId, String customerId, BigDecimal amount, Exception ex) {
        log.error("Circuit breaker opened for payment. Error: {}", ex.getMessage());
        publishPaymentFailedEvent(orderId, "Payment service temporarily unavailable");
    }

    /**
     * Publish payment failed event
     */
    public void publishPaymentFailedEvent(String orderId, String reason) {
        String transactionId = UUID.randomUUID().toString();
        paymentEventProducer.publishPaymentProcessedEvent(
                orderId,
                transactionId,
                BigDecimal.ZERO,
                "FAILED",
                reason
        );
    }

    /**
     * Get payment by transaction ID
     */
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        log.info("Fetching payment with transactionId: {}", transactionId);
        return paymentRepository.findByTransactionId(transactionId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + transactionId));
    }

    /**
     * Get payment by order ID
     */
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOrderId(String orderId) {
        log.info("Fetching payment with orderId: {}", orderId);
        return paymentRepository.findByOrderId(orderId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    /**
     * Simulate payment processing (70% success rate)
     */
    private boolean simulatePaymentProcessing() {
        try {
            // Simulate payment gateway delay
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 70% success rate
        return random.nextInt(100) < 70;
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Convert Payment entity to PaymentDTO
     */
    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().toString())
                .status(payment.getStatus().toString())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
