package com.youtube.channelservice.infrastructure.persistence.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.Instant;

/**
 * Cosmos DB entity for Subscription.
 * Partition key: shardSuffix (for anti-supernode strategy).
 */
@Container(containerName = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEntity {
    
    @Id
    private String id;
    
    @PartitionKey
    private String shardSuffix;
    
    private String userId;
    
    private String channelId;
    
    private Boolean isActive;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    // Serialized JSON for notification preferences
    private String notificationPreferenceJson;
    
    @Version
    private String etag;
}
