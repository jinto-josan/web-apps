package com.youtube.mediaassist.domain.repositories;

import java.util.Optional;

/**
 * Repository for idempotency key tracking
 */
public interface IdempotencyRepository {
    
    /**
     * Store the result of an idempotent operation
     */
    void store(String idempotencyKey, String result, long ttlSeconds);
    
    /**
     * Retrieve the result of a previous idempotent operation
     */
    Optional<String> retrieve(String idempotencyKey);
    
    /**
     * Check if key exists
     */
    boolean exists(String idempotencyKey);
    
    /**
     * Delete idempotency key (cleanup)
     */
    void delete(String idempotencyKey);
}

