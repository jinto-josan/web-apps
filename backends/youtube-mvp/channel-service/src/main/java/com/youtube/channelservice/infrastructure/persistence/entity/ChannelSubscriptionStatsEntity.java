package com.youtube.channelservice.infrastructure.persistence.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.Instant;

/**
 * Cosmos DB read model for channel subscription statistics.
 */
@Container(containerName = "channel-subscription-stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSubscriptionStatsEntity {
    
    @Id
    private String channelId;
    
    private Long subscriberCount;
    
    private Long activeSubscriberCount;
    
    private Instant lastUpdatedAt;
    
    private Instant lastSubscriberAddedAt;
    
    @Version
    private String etag;
}
