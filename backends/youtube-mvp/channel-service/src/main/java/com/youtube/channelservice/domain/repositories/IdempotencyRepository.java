package com.youtube.channelservice.domain.repositories;

import java.time.Duration;
import java.util.Optional;

/**
 * Repository for idempotency key tracking.
 * Prevents duplicate processing of the same request.
 */
public interface IdempotencyRepository {
    
    /**
     * Check if an idempotency key exists.
     * 
     * @param key The idempotency key
     * @return Optional containing the cached response if the key exists
     */
    Optional<String> get(String key);
    
    /**
     * Store an idempotency key with a response.
     * 
     * @param key The idempotency key
     * @param response The response to cache
     * @param ttl TTL for the key
     */
    void put(String key, String response, Duration ttl);
    
    /**
     * Check and set (CAS) operation for idempotency.
     * Returns true if the key was set, false if it already existed.
     */
    boolean checkAndSet(String key, String response, Duration ttl);
}
