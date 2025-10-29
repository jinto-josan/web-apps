package com.youtube.commentsservice.domain.services;

import java.util.Optional;

/**
 * Port for idempotency checking
 * Implemented by infrastructure layer using Redis
 */
public interface IdempotencyCheckerPort {
    
    /**
     * Check if request is duplicate based on idempotency key
     * @param idempotencyKey the idempotency key
     * @return optional result if request was already processed
     */
    Optional<String> checkDuplicate(String idempotencyKey);
    
    /**
     * Store idempotency result
     * @param idempotencyKey the idempotency key
     * @param result the result to store
     * @param ttlSeconds time to live in seconds
     */
    void storeResult(String idempotencyKey, String result, long ttlSeconds);
}

