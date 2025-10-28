package com.youtube.channelservice.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a user subscribes to a channel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreated {
    
    private String subscriptionId;
    private String userId;
    private String channelId;
    private String channelOwnerId;
    private Instant subscribedAt;
    private String shardSuffix;
}
