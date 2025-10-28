package com.youtube.mvp.streaming.infrastructure.persistence.redis;

import com.youtube.mvp.streaming.domain.model.PlaybackSession;
import com.youtube.mvp.streaming.domain.repository.PlaybackSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis implementation of PlaybackSessionRepository.
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class PlaybackSessionRedisRepository implements PlaybackSessionRepository {
    
    private final RedisTemplate<String, PlaybackSession> redisTemplate;
    
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String VIDEO_USER_KEY_PREFIX = "video-user:";
    private static final Duration SESSION_TTL = Duration.ofHours(2);
    
    @Override
    public PlaybackSession save(PlaybackSession session) {
        log.debug("Saving session: {}", session.getSessionId());
        
        // Save by session ID
        String sessionKey = SESSION_KEY_PREFIX + session.getSessionId();
        redisTemplate.opsForValue().set(sessionKey, session, SESSION_TTL);
        
        // Save by video-user for quick lookup
        String videoUserKey = VIDEO_USER_KEY_PREFIX + session.getVideoId() + ":" + session.getUserId();
        redisTemplate.opsForValue().set(videoUserKey, session.getSessionId(), SESSION_TTL);
        
        log.debug("Session saved successfully");
        return session;
    }
    
    @Override
    public Optional<PlaybackSession> findById(String sessionId) {
        log.debug("Finding session by ID: {}", sessionId);
        
        String key = SESSION_KEY_PREFIX + sessionId;
        PlaybackSession session = redisTemplate.opsForValue().get(key);
        
        if (session == null) {
            return Optional.empty();
        }
        
        return Optional.of(session);
    }
    
    @Override
    public Optional<PlaybackSession> findByVideoIdAndUserId(String videoId, String userId) {
        log.debug("Finding session by video: {}, user: {}", videoId, userId);
        
        String videoUserKey = VIDEO_USER_KEY_PREFIX + videoId + ":" + userId;
        String sessionId = (String) redisTemplate.opsForValue().get(videoUserKey);
        
        if (sessionId == null) {
            return Optional.empty();
        }
        
        return findById(sessionId);
    }
    
    @Override
    public boolean existsById(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Override
    public void deleteById(String sessionId) {
        log.debug("Deleting session: {}", sessionId);
        
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        PlaybackSession session = redisTemplate.opsForValue().get(sessionKey);
        
        if (session != null) {
            String videoUserKey = VIDEO_USER_KEY_PREFIX + session.getVideoId() + ":" + session.getUserId();
            redisTemplate.delete(videoUserKey);
        }
        
        redisTemplate.delete(sessionKey);
    }
}

