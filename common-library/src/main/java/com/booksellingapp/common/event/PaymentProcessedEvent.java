package com.booksellingapp.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Event published when payment is processed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String transactionId;
    private BigDecimal amount;
    private String status; // SUCCESS, FAILED
    private String message;
    private Long timestamp;
}
