package com.youtube.livechatservice.infrastructure.external;

import com.youtube.livechatservice.domain.entities.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisServices {
    private final StringRedisTemplate redisTemplate;

    public boolean tryIdempotency(String key, Duration ttl) {
        Boolean set = redisTemplate.opsForValue().setIfAbsent("idemp:" + key, "1", ttl);
        return Boolean.TRUE.equals(set);
    }

    public void pushRecentMessage(String liveId, ChatMessage message, int maxSize, Duration ttl) {
        String listKey = "recent:" + liveId;
        redisTemplate.opsForList().leftPush(listKey, serialize(message));
        redisTemplate.opsForList().trim(listKey, 0, maxSize - 1);
        redisTemplate.expire(listKey, ttl);
    }

    public List<String> getRecentRaw(String liveId, int limit) {
        String listKey = "recent:" + liveId;
        return redisTemplate.opsForList().range(listKey, 0, limit - 1);
    }

    private String serialize(ChatMessage m) {
        return String.join("|",
                m.getMessageId(), m.getLiveId().getValue(), m.getUserId(), m.getDisplayName(),
                m.getContent(), String.valueOf(m.getCreatedAt().toEpochMilli()));
    }
}


