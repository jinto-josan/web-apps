package com.youtube.channelservice.infrastructure.config;

import com.youtube.channelservice.interfaces.persistence.CacheAdapter;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

public class RedisCacheAdapter implements CacheAdapter {  
    private final StringRedisTemplate redis;  
  
    public RedisCacheAdapter(StringRedisTemplate redis) { this.redis = redis; }  
  
    @Override  
    public void putHandleMapping(String handleLower, String channelId) {  
        redis.opsForValue().set(key(handleLower), channelId, Duration.ofHours(6));  
    }  
  
    @Override  
    public String getChannelIdByHandle(String handleLower) {  
        return redis.opsForValue().get(key(handleLower));  
    }  
  
    @Override  
    public void invalidateHandleMapping(String handleLower) {  
        redis.delete(key(handleLower));  
    }  
  
    @Override  
    public void invalidatePermissions(String channelId, String userId) {  
        redis.delete("channel-perm:" + channelId + ":" + userId);  
    }  
  
    private String key(String h) { return "handle:" + h; }  
}  