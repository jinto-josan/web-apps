package com.youtube.channelservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Read model for channel subscription statistics.
 * CQRS read model for efficient querying.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSubscriptionStats {
    
    private String channelId;
    private Long subscriberCount;
    private Long activeSubscriberCount;
    private Instant lastUpdatedAt;
    private Instant lastSubscriberAddedAt;
}
