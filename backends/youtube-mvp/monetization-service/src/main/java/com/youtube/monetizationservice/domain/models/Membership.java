package com.youtube.monetizationservice.domain.models;

import com.youtube.monetizationservice.domain.valueobjects.MembershipTier;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Domain entity representing a membership subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {
    
    @NotBlank(message = "Membership ID cannot be blank")
    private String id; // ULID
    
    @NotBlank(message = "Channel ID cannot be blank")
    private String channelId;
    
    @NotBlank(message = "Subscriber ID cannot be blank")
    private String subscriberId;
    
    @NotNull(message = "Tier cannot be null")
    private MembershipTier tier;
    
    @NotNull(message = "Monthly fee cannot be null")
    private Money monthlyFee;
    
    @NotNull(message = "Status cannot be null")
    private MembershipStatus status;
    
    @NotNull(message = "Start date cannot be null")
    private Instant startDate;
    
    private Instant endDate;
    private Instant nextBillingDate;
    
    private String paymentMethodId;
    private String externalSubscriptionId; // Payment provider subscription ID
    
    @Builder.Default
    private int version = 1;
    
    @NotNull(message = "Created at timestamp cannot be null")
    private Instant createdAt;
    
    @NotNull(message = "Updated at timestamp cannot be null")
    private Instant updatedAt;
    
    private String etag; // For optimistic concurrency control
    
    /**
     * Membership status enumeration.
     */
    public enum MembershipStatus {
        ACTIVE,
        SUSPENDED,
        CANCELLED,
        EXPIRED
    }
    
    /**
     * Creates a new membership with updated status.
     */
    public Membership withStatus(MembershipStatus newStatus, int newVersion, Instant updatedAt, String newEtag) {
        return this.toBuilder()
                .status(newStatus)
                .version(newVersion)
                .updatedAt(updatedAt)
                .etag(newEtag)
                .build();
    }
    
    /**
     * Creates a new membership with updated billing date.
     */
    public Membership withNextBillingDate(Instant newBillingDate, int newVersion, Instant updatedAt, String newEtag) {
        return this.toBuilder()
                .nextBillingDate(newBillingDate)
                .version(newVersion)
                .updatedAt(updatedAt)
                .etag(newEtag)
                .build();
    }
    
    /**
     * Checks if membership is active.
     */
    public boolean isActive() {
        return status == MembershipStatus.ACTIVE && 
               (endDate == null || endDate.isAfter(Instant.now()));
    }
}

