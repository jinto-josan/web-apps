package com.youtube.mediaassist.infrastructure.persistence;

import com.youtube.mediaassist.domain.repositories.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-based idempotency repository implementation
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisIdempotencyRepository implements IdempotencyRepository {
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public void store(String idempotencyKey, String result, long ttlSeconds) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(ttlSeconds));
        log.debug("Stored idempotency key: {}", idempotencyKey);
    }
    
    @Override
    public Optional<String> retrieve(String idempotencyKey) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        String result = redisTemplate.opsForValue().get(key);
        log.debug("Retrieved idempotency key: {}", idempotencyKey);
        return Optional.ofNullable(result);
    }
    
    @Override
    public boolean exists(String idempotencyKey) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    
    @Override
    public void delete(String idempotencyKey) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.delete(key);
        log.debug("Deleted idempotency key: {}", idempotencyKey);
    }
}

