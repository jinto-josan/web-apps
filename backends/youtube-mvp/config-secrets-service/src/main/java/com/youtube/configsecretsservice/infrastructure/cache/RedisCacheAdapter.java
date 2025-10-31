package com.youtube.configsecretsservice.infrastructure.cache;

import com.youtube.configsecretsservice.domain.port.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Adapter implementing CachePort using Redis.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements CachePort {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Optional<String> get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.error("Error getting from cache: {}", key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void put(String key, String value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.error("Error putting to cache: {}", key, e);
        }
    }
    
    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error evicting from cache: {}", key, e);
        }
    }
    
    @Override
    public void evictByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Error evicting by pattern from cache: {}", pattern, e);
        }
    }
}

