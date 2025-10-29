package com.youtube.livestreaming.infrastructure.adapters.persistence;

import com.youtube.livestreaming.domain.ports.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-based idempotency service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyService implements IdempotencyService {
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public Optional<String> processIdempotencyKey(String idempotencyKey) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        String requestId = redisTemplate.opsForValue().get(key);
        
        if (requestId != null) {
            log.info("Idempotency key found: {}", idempotencyKey);
            return Optional.of(requestId);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void storeRequestId(String idempotencyKey, String requestId) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, requestId, IDEMPOTENCY_TTL);
        log.info("Stored idempotency key: {}", idempotencyKey);
    }
    
    @Override
    public boolean isRequestAlreadyProcessed(String idempotencyKey) {
        return processIdempotencyKey(idempotencyKey).isPresent();
    }
}

