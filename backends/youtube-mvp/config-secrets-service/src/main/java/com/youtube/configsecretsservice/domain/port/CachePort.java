package com.youtube.configsecretsservice.domain.port;

import java.util.Optional;

/**
 * Port for caching operations.
 */
public interface CachePort {
    Optional<String> get(String key);
    void put(String key, String value, long ttlSeconds);
    void evict(String key);
    void evictByPattern(String pattern);
}

