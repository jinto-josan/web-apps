package com.youtube.channelservice.infrastructure.persistence.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.youtube.channelservice.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Cosmos DB repository for subscriptions.
 */
public interface CosmosSubscriptionRepository extends CosmosRepository<SubscriptionEntity, String> {
    
    List<SubscriptionEntity> findByUserIdAndShardSuffix(
        @Param("userId") String userId,
        @Param("shardSuffix") String shardSuffix
    );
    
    Optional<SubscriptionEntity> findByUserIdAndChannelId(
        @Param("userId") String userId,
        @Param("channelId") String channelId
    );
    
    List<SubscriptionEntity> findByChannelId(
        @Param("channelId") String channelId
    );
    
    long countByChannelId(String channelId);
}
