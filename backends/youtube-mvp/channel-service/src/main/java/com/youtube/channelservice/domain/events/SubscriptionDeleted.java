package com.youtube.channelservice.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a user unsubscribes from a channel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDeleted {
    
    private String subscriptionId;
    private String userId;
    private String channelId;
    private Instant unsubscribedAt;
    private String reason;
}
