package com.youtube.channelservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Query to get all subscriptions for a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserSubscriptionsQuery implements Query {
    
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    @Builder.Default
    @Min(value = 0, message = "Offset must be non-negative")
    private int offset = 0;
    
    @Builder.Default
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    private int limit = 20;
    
    private Boolean includeInactive;
    
    // For anti-supernode strategy
    private String shardSuffix;
}
