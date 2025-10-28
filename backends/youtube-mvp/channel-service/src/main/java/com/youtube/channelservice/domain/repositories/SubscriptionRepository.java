package com.youtube.channelservice.domain.repositories;

import com.youtube.channelservice.domain.models.Subscription;

import java.util.List;
import java.util.Optional;

/**
 * Repository for subscription aggregates.
 * Port for subscription persistence.
 */
public interface SubscriptionRepository {
    
    /**
     * Find subscription by ID.
     */
    Optional<Subscription> findById(String subscriptionId);
    
    /**
     * Find all active subscriptions for a user with pagination support.
     * Uses shard suffix for anti-supernode strategy.
     */
    List<Subscription> findByUserIdWithShard(String userId, String shardSuffix, int offset, int limit);
    
    /**
     * Find subscription by user and channel.
     */
    Optional<Subscription> findByUserIdAndChannelId(String userId, String channelId);
    
    /**
     * Find all subscriptions for a channel (for notifications/fan-out).
     */
    List<Subscription> findByChannelId(String channelId, int offset, int limit);
    
    /**
     * Count active subscriptions for a user.
     */
    long countByUserId(String userId);
    
    /**
     * Count active subscriptions for a channel.
     */
    long countByChannelId(String channelId);
    
    /**
     * Save or update a subscription.
     */
    Subscription save(Subscription subscription);
    
    /**
     * Delete a subscription.
     */
    void delete(Subscription subscription);
    
    /**
     * Delete subscription by ID (idempotent).
     */
    void deleteById(String subscriptionId);
}
