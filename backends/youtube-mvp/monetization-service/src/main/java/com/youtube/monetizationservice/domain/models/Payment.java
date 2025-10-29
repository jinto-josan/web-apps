package com.youtube.monetizationservice.domain.models;

import com.youtube.monetizationservice.domain.valueobjects.Money;
import com.youtube.monetizationservice.domain.valueobjects.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Domain entity representing a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @NotBlank(message = "Payment ID cannot be blank")
    private String id; // ULID
    
    @NotBlank(message = "Invoice ID cannot be blank")
    private String invoiceId;
    
    @NotBlank(message = "Membership ID cannot be blank")
    private String membershipId;
    
    @NotBlank(message = "Payment method ID cannot be blank")
    private String paymentMethodId;
    
    @NotNull(message = "Amount cannot be null")
    private Money amount;
    
    @NotNull(message = "Status cannot be null")
    private PaymentStatus status;
    
    @NotBlank(message = "External payment ID cannot be blank")
    private String externalPaymentId; // Payment provider transaction ID
    
    @NotNull(message = "Transaction date cannot be null")
    private Instant transactionDate;
    
    private String failureReason;
    private Instant refundedDate;
    
    @Builder.Default
    private int version = 1;
    
    @NotNull(message = "Created at timestamp cannot be null")
    private Instant createdAt;
    
    @NotNull(message = "Updated at timestamp cannot be null")
    private Instant updatedAt;
    
    private String etag; // For optimistic concurrency control
    
    /**
     * Creates a new payment with updated status.
     */
    public Payment withStatus(PaymentStatus newStatus, int newVersion, Instant updatedAt, String newEtag) {
        return this.toBuilder()
                .status(newStatus)
                .version(newVersion)
                .updatedAt(updatedAt)
                .etag(newEtag)
                .build();
    }
}

