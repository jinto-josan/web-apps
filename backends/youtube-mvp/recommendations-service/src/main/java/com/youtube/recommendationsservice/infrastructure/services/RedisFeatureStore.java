package com.youtube.recommendationsservice.infrastructure.services;

import com.youtube.recommendationsservice.domain.services.FeatureStore;
import com.youtube.recommendationsservice.domain.valueobjects.FeatureVector;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import com.youtube.recommendationsservice.domain.valueobjects.VideoId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisFeatureStore implements FeatureStore {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String USER_PREFIX = "features:user:";
    private static final String VIDEO_PREFIX = "features:video:";
    
    @Override
    public Optional<FeatureVector> getUserFeatures(UserId userId) {
        String key = USER_PREFIX + userId.getValue();
        @SuppressWarnings("unchecked")
        FeatureVector features = (FeatureVector) redisTemplate.opsForValue().get(key);
        
        if (features != null) {
            log.debug("Cache hit for user features: {}", userId.getValue());
        } else {
            log.debug("Cache miss for user features: {}", userId.getValue());
        }
        
        return Optional.ofNullable(features);
    }
    
    @Override
    public Optional<FeatureVector> getVideoFeatures(VideoId videoId) {
        String key = VIDEO_PREFIX + videoId.getValue();
        @SuppressWarnings("unchecked")
        FeatureVector features = (FeatureVector) redisTemplate.opsForValue().get(key);
        
        if (features != null) {
            log.debug("Cache hit for video features: {}", videoId.getValue());
        } else {
            log.debug("Cache miss for video features: {}", videoId.getValue());
        }
        
        return Optional.ofNullable(features);
    }
    
    @Override
    public void cacheUserFeatures(UserId userId, FeatureVector features) {
        String key = USER_PREFIX + userId.getValue();
        redisTemplate.opsForValue().set(key, features, Duration.ofHours(24));
        log.debug("Cached user features for: {}", userId.getValue());
    }
    
    @Override
    public void cacheVideoFeatures(VideoId videoId, FeatureVector features) {
        String key = VIDEO_PREFIX + videoId.getValue();
        redisTemplate.opsForValue().set(key, features, Duration.ofHours(24));
        log.debug("Cached video features for: {}", videoId.getValue());
    }
}

