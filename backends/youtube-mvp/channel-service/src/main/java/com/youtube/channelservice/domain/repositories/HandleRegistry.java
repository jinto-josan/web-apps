package com.youtube.channelservice.domain.repositories;

import java.time.Duration;
import java.util.Optional;

/**
 * Repository interface for Handle Registry.
 * Defines the contract for handle reservation and management operations.
 */
public interface HandleRegistry {
    
    /**
     * Reserves a handle for a user with a TTL.
     * @param handleLower The handle in lowercase
     * @param userId The user ID requesting the reservation
     * @param ttl The time-to-live for the reservation
     * @return true if reservation was successful, false if handle is already taken
     */
    boolean reserve(String handleLower, String userId, Duration ttl);
    
    /**
     * Commits a reserved handle to a channel.
     * @param handleLower The handle in lowercase
     * @param channelId The channel ID to associate with the handle
     * @return true if commit was successful, false if handle is not reserved or expired
     */
    boolean commit(String handleLower, String channelId);
    
    /**
     * Releases a reserved handle.
     * @param handleLower The handle in lowercase
     * @return true if release was successful, false if handle was not reserved
     */
    boolean release(String handleLower);
    
    /**
     * Looks up the channel ID associated with a handle.
     * @param handleLower The handle in lowercase
     * @return Optional containing the channel ID if the handle is committed
     */
    Optional<String> lookupChannelId(String handleLower);
}
