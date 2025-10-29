package com.youtube.monetizationservice.application.dto;

import com.youtube.monetizationservice.domain.valueobjects.MembershipTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for membership response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipResponse {
    private String id;
    private String channelId;
    private String subscriberId;
    private MembershipTier tier;
    private BigDecimal monthlyFeeAmount;
    private String currency;
    private String status;
    private Instant startDate;
    private Instant endDate;
    private Instant nextBillingDate;
    private Instant createdAt;
    private Instant updatedAt;
}

