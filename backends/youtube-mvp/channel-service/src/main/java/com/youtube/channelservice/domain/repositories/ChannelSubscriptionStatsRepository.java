package com.youtube.channelservice.domain.repositories;

import com.youtube.channelservice.domain.models.ChannelSubscriptionStats;

import java.util.Optional;

/**
 * Read model repository for channel subscription statistics.
 * CQRS read model for efficient subscription count queries.
 */
public interface ChannelSubscriptionStatsRepository {
    
    /**
     * Find subscription stats by channel ID.
     */
    Optional<ChannelSubscriptionStats> findByChannelId(String channelId);
    
    /**
     * Save or update subscription stats.
     */
    ChannelSubscriptionStats save(ChannelSubscriptionStats stats);
    
    /**
     * Increment subscriber count atomically.
     */
    void incrementSubscriberCount(String channelId);
    
    /**
     * Decrement subscriber count atomically.
     */
    void decrementSubscriberCount(String channelId);
}
