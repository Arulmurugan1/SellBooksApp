package com.booksellingapp.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private String transactionId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String failureReason;
}
