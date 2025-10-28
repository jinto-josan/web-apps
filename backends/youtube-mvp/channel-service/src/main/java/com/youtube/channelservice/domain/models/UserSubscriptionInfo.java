package com.youtube.channelservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

/**
 * Read model for user subscription information.
 * CQRS read model for efficient user subscription queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionInfo {
    
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    @NotNull(message = "Subscriptions cannot be null")
    private List<SubscriptionSummary> subscriptions;
    
    private Long totalCount;
    
    @NotNull(message = "Updated at cannot be null")
    private Instant updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionSummary {
        private String channelId;
        private String channelHandle;
        private String channelTitle;
        private Instant subscribedAt;
        private Subscription.SubscriptionNotificationPreference notificationPreference;
    }
}
