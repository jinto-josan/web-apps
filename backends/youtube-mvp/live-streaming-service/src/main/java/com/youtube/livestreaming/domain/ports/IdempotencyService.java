package com.youtube.livestreaming.domain.ports;

import java.util.Optional;

/**
 * Service for handling idempotency using Idempotency-Key header
 */
public interface IdempotencyService {
    Optional<String> processIdempotencyKey(String idempotencyKey);
    
    void storeRequestId(String idempotencyKey, String requestId);
    
    boolean isRequestAlreadyProcessed(String idempotencyKey);
}

