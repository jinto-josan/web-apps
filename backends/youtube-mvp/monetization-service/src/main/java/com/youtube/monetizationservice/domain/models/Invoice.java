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
import java.util.Map;

/**
 * Domain entity representing an invoice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    
    @NotBlank(message = "Invoice ID cannot be blank")
    private String id; // ULID
    
    @NotBlank(message = "Membership ID cannot be blank")
    private String membershipId;
    
    @NotBlank(message = "Channel ID cannot be blank")
    private String channelId;
    
    @NotBlank(message = "Subscriber ID cannot be blank")
    private String subscriberId;
    
    @NotNull(message = "Amount cannot be null")
    private Money amount;
    
    @NotNull(message = "Status cannot be null")
    private PaymentStatus status;
    
    @NotNull(message = "Due date cannot be null")
    private Instant dueDate;
    
    private Instant paidDate;
    private String paymentId;
    
    @Builder.Default
    private Map<String, String> metadata = Map.of();
    
    @Builder.Default
    private int version = 1;
    
    @NotNull(message = "Created at timestamp cannot be null")
    private Instant createdAt;
    
    @NotNull(message = "Updated at timestamp cannot be null")
    private Instant updatedAt;
    
    private String etag; // For optimistic concurrency control
    
    /**
     * Creates a new invoice with updated status.
     */
    public Invoice withStatus(PaymentStatus newStatus, int newVersion, Instant updatedAt, String newEtag) {
        return this.toBuilder()
                .status(newStatus)
                .version(newVersion)
                .updatedAt(updatedAt)
                .etag(newEtag)
                .build();
    }
}
