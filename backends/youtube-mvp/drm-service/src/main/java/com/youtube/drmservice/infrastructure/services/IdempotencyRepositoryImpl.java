package com.youtube.drmservice.infrastructure.services;

import com.youtube.drmservice.domain.services.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyRepositoryImpl implements IdempotencyRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    @Override
    public boolean isIdempotent(String key) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + key;
        Boolean exists = redisTemplate.hasKey(redisKey);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void markIdempotent(String key, long ttlSeconds) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, "1", ttlSeconds, TimeUnit.SECONDS);
        log.debug("Marked idempotency key: {}", key);
    }
}

