package com.youtube.monetizationservice.application.dto;

import com.youtube.monetizationservice.domain.valueobjects.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * DTO for invoice response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private String id;
    private String membershipId;
    private String channelId;
    private String subscriberId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private Instant dueDate;
    private Instant paidDate;
    private Instant createdAt;
    private Map<String, String> metadata;
}

