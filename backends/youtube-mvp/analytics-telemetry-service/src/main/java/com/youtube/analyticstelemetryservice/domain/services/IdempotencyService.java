package com.youtube.analyticstelemetryservice.domain.services;

/**
 * Domain service for idempotency checking.
 * Uses Redis to store processed idempotency keys.
 */
public interface IdempotencyService {
    
    /**
     * Check if a request with the given idempotency key has already been processed.
     * @param idempotencyKey the idempotency key
     * @return true if already processed
     */
    boolean isProcessed(String idempotencyKey);
    
    /**
     * Mark an idempotency key as processed.
     * @param idempotencyKey the idempotency key
     */
    void markProcessed(String idempotencyKey);
    
    /**
     * Check if an event ID has been processed (for individual event idempotency).
     * @param eventId the event ID
     * @return true if already processed
     */
    boolean isEventProcessed(String eventId);
    
    /**
     * Mark an event ID as processed.
     * @param eventId the event ID
     */
    void markEventProcessed(String eventId);
}

