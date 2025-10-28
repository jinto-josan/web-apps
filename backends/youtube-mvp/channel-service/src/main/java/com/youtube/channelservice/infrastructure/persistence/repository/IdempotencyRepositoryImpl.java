package com.youtube.channelservice.infrastructure.persistence.repository;

import com.youtube.channelservice.domain.repositories.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IdempotencyRepositoryImpl implements IdempotencyRepository {
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Optional<String> get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(IDEMPOTENCY_KEY_PREFIX + key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("Error getting idempotency key from Redis", e);
            return Optional.empty();
        }
    }
    
    @Override
    public void put(String key, String response, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(
                IDEMPOTENCY_KEY_PREFIX + key,
                response,
                ttl
            );
        } catch (Exception e) {
            log.error("Error storing idempotency key in Redis", e);
            // Don't throw, allow the operation to continue
        }
    }
    
    @Override
    public boolean checkAndSet(String key, String response, Duration ttl) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(
                IDEMPOTENCY_KEY_PREFIX + key,
                response,
                ttl
            );
            return result != null && result;
        } catch (Exception e) {
            log.error("Error checking and setting idempotency key in Redis", e);
            return false;
        }
    }
}
