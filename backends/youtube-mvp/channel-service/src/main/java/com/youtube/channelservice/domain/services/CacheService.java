package com.youtube.channelservice.domain.services;

/**
 * Domain service interface for caching operations.
 * Defines the contract for caching channel-related data.
 */
public interface CacheService {
    
    /**
     * Stores a handle-to-channel mapping in the cache.
     * @param handleLower The handle in lowercase
     * @param channelId The channel ID
     */
    void putHandleMapping(String handleLower, String channelId);
    
    /**
     * Retrieves a channel ID by handle from the cache.
     * @param handleLower The handle in lowercase
     * @return The channel ID if found in cache, null otherwise
     */
    String getChannelIdByHandle(String handleLower);
    
    /**
     * Invalidates a handle mapping from the cache.
     * @param handleLower The handle in lowercase
     */
    void invalidateHandleMapping(String handleLower);
    
    /**
     * Invalidates permission cache for a user in a channel.
     * @param channelId The channel ID
     * @param userId The user ID
     */
    void invalidatePermissions(String channelId, String userId);
}
