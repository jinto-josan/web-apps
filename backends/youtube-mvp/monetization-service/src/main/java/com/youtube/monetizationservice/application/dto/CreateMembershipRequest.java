package com.youtube.monetizationservice.application.dto;

import com.youtube.monetizationservice.domain.valueobjects.MembershipTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO for creating a membership subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMembershipRequest {
    
    @NotBlank(message = "Channel ID is required")
    private String channelId;
    
    @NotNull(message = "Tier is required")
    private MembershipTier tier;
    
    @NotNull(message = "Monthly fee amount is required")
    private BigDecimal monthlyFeeAmount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;
}

