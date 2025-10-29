package com.youtube.monetizationservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value object representing payment status.
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING("Payment is pending"),
    PROCESSING("Payment is being processed"),
    COMPLETED("Payment completed successfully"),
    FAILED("Payment failed"),
    REFUNDED("Payment was refunded"),
    CANCELLED("Payment was cancelled");

    private final String description;
}

