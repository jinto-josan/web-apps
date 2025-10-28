package com.youtube.channelservice.infrastructure.persistence.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.youtube.channelservice.infrastructure.persistence.entity.ChannelSubscriptionStatsEntity;
import org.springframework.data.repository.query.Param;

/**
 * Cosmos DB read model repository for channel subscription statistics.
 */
public interface CosmosChannelSubscriptionStatsRepository 
    extends CosmosRepository<ChannelSubscriptionStatsEntity, String> {
    
    ChannelSubscriptionStatsEntity findByChannelId(@Param("channelId") String channelId);
}
