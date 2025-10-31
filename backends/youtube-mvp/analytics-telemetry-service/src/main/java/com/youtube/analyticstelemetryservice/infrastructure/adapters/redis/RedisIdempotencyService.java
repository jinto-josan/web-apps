package com.youtube.analyticstelemetryservice.infrastructure.adapters.redis;

import com.youtube.analyticstelemetryservice.domain.services.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Infrastructure adapter for idempotency service using Redis.
 * Stores processed idempotency keys and event IDs in Redis with TTL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisIdempotencyService implements IdempotencyService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Value("${telemetry.idempotency.ttl-hours:24}")
    private int ttlHours;
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final String EVENT_KEY_PREFIX = "event:";
    
    @Override
    public boolean isProcessed(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    
    @Override
    public void markProcessed(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, "processed", Duration.ofHours(ttlHours));
        log.debug("Marked idempotency key as processed: {}", idempotencyKey);
    }
    
    @Override
    public boolean isEventProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        String key = EVENT_KEY_PREFIX + eventId;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    
    @Override
    public void markEventProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        String key = EVENT_KEY_PREFIX + eventId;
        redisTemplate.opsForValue().set(key, "processed", Duration.ofHours(ttlHours));
        log.debug("Marked event as processed: {}", eventId);
    }
}

