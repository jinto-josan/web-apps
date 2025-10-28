package com.youtube.drmservice.infrastructure.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.services.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${drm.cache.ttl:3600}")
    private long cacheTtlSeconds;

    private static final String CACHE_KEY_PREFIX = "drm:policy:";
    private static final String VIDEO_CACHE_KEY_PREFIX = "drm:policy:video:";

    @Override
    public Optional<DrmPolicy> getPolicy(String policyId) {
        try {
            String key = CACHE_KEY_PREFIX + policyId;
            String json = redisTemplate.opsForValue().get(key);
            
            if (json != null) {
                DrmPolicy policy = objectMapper.readValue(json, DrmPolicy.class);
                log.debug("Cache hit for policy: {}", policyId);
                return Optional.of(policy);
            }
            
            log.debug("Cache miss for policy: {}", policyId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error reading from cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void putPolicy(DrmPolicy policy) {
        try {
            String key = CACHE_KEY_PREFIX + policy.getId();
            String videoKey = VIDEO_CACHE_KEY_PREFIX + policy.getVideoId();
            String json = objectMapper.writeValueAsString(policy);
            
            redisTemplate.opsForValue().set(key, json, cacheTtlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(videoKey, json, cacheTtlSeconds, TimeUnit.SECONDS);
            
            log.debug("Cached policy: {}", policy.getId());
        } catch (Exception e) {
            log.error("Error caching policy", e);
        }
    }

    @Override
    public void evictPolicy(String policyId) {
        redisTemplate.delete(CACHE_KEY_PREFIX + policyId);
        log.debug("Evicted policy from cache: {}", policyId);
    }

    @Override
    public void evictPolicyByVideoId(String videoId) {
        redisTemplate.delete(VIDEO_CACHE_KEY_PREFIX + videoId);
        redisTemplate.delete(CACHE_KEY_PREFIX + videoId);
        log.debug("Evicted policy from cache by video ID: {}", videoId);
    }
}

