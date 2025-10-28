package com.youtube.channelservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Represents a subscription to a channel.
 * Implements anti-supernode sharding by user suffix.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    
    @NotBlank(message = "Subscription ID cannot be blank")
    private String id; // ULID
    
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    @NotBlank(message = "Channel ID cannot be blank")
    private String channelId;
    
    @NotBlank(message = "Shard suffix cannot be blank")
    private String shardSuffix; // For anti-supernode strategy
    
    @NotNull(message = "Created at timestamp cannot be null")
    private Instant createdAt;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private SubscriptionNotificationPreference notificationPreference;
    
    /**
     * Gets the shard key based on user ID suffix (last 2 chars).
     * Anti-supernode strategy to distribute subscriptions.
     */
    public static String calculateShardSuffix(String userId) {
        if (userId == null || userId.length() < 2) {
            return "00";
        }
        String lastTwoChars = userId.substring(userId.length() - 2).toLowerCase();
        return lastTwoChars;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionNotificationPreference {
        private Boolean notifyOnUpload = true;
        private Boolean notifyOnLive = true;
        private Boolean notifyOnCommunityPost = true;
        private Boolean notifyOnShorts = true;
    }
}
