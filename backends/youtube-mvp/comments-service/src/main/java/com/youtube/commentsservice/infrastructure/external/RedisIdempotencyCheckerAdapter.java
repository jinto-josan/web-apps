package com.youtube.commentsservice.infrastructure.external;

import com.youtube.commentsservice.domain.services.IdempotencyCheckerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisIdempotencyCheckerAdapter implements IdempotencyCheckerPort {
    
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Optional<String> checkDuplicate(String idempotencyKey) {
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        String existing = redisTemplate.opsForValue().get(key);
        
        if (existing != null) {
            log.debug("Idempotency key found: {}", idempotencyKey);
            return Optional.of(existing);
        }
        
        return Optional.empty();
    }
    
    @Override
    public void storeResult(String idempotencyKey, String result, long ttlSeconds) {
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, result, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Stored idempotency result for key: {}", idempotencyKey);
    }
}

