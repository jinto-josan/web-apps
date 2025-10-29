package com.youtube.monetizationservice.domain.repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for managing idempotency keys to prevent duplicate processing.
 */
public interface IdempotencyRepository {
    
    /**
     * Stores an idempotency key with expiration.
     * 
     * @param key the idempotency key
     * @param result the result to store (for GET requests)
     * @param expiresAt expiration timestamp
     * @return true if key was newly created, false if already exists
     */
    boolean store(String key, String result, Instant expiresAt);
    
    /**
     * Retrieves stored result for an idempotency key.
     * 
     * @param key the idempotency key
     * @return optional result if key exists and not expired
     */
    Optional<String> get(String key);
    
    /**
     * Deletes an idempotency key.
     * 
     * @param key the idempotency key
     */
    void delete(String key);
}

