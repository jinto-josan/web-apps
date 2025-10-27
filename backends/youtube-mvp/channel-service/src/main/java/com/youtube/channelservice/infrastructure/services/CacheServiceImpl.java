package com.youtube.channelservice.infrastructure.services;

import com.youtube.channelservice.domain.services.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Infrastructure implementation of CacheService using Redis.
 * Provides caching functionality for channel-related data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public void putHandleMapping(String handleLower, String channelId) {
        String key = "handle:" + handleLower;
        redisTemplate.opsForValue().set(key, channelId, Duration.ofHours(6));
        log.debug("Cached handle mapping: {} -> {}", handleLower, channelId);
    }
    
    @Override
    public String getChannelIdByHandle(String handleLower) {
        String key = "handle:" + handleLower;
        String channelId = redisTemplate.opsForValue().get(key);
        log.debug("Retrieved channel ID from cache: {} -> {}", handleLower, channelId);
        return channelId;
    }
    
    @Override
    public void invalidateHandleMapping(String handleLower) {
        String key = "handle:" + handleLower;
        redisTemplate.delete(key);
        log.debug("Invalidated handle mapping cache: {}", handleLower);
    }
    
    @Override
    public void invalidatePermissions(String channelId, String userId) {
        String key = "channel-perm:" + channelId + ":" + userId;
        redisTemplate.delete(key);
        log.debug("Invalidated permissions cache for user {} in channel {}", userId, channelId);
    }
}
