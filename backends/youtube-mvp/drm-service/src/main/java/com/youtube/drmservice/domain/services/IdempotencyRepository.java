package com.youtube.drmservice.domain.services;

/**
 * Port for idempotency key tracking
 */
public interface IdempotencyRepository {
    boolean isIdempotent(String key);
    void markIdempotent(String key, long ttlSeconds);
}

