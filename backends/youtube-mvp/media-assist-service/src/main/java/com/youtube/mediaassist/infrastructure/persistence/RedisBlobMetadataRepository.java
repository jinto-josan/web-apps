package com.youtube.mediaassist.infrastructure.persistence;

import com.youtube.mediaassist.domain.repositories.BlobMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-based blob metadata cache repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisBlobMetadataRepository implements BlobMetadataRepository {
    
    private static final String METADATA_KEY_PREFIX = "blob:metadata:";
    private static final long DEFAULT_TTL = 3600; // 1 hour
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean exists(String blobPath) {
        String key = METADATA_KEY_PREFIX + blobPath;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    
    @Override
    public Optional<BlobMetadata> findByPath(String blobPath) {
        String key = METADATA_KEY_PREFIX + blobPath;
        String json = redisTemplate.opsForValue().get(key);
        
        if (json == null) {
            return Optional.empty();
        }
        
        // Simple deserialization - in production use proper JSON library
        // For now, storing path only
        return Optional.of(new BlobMetadata(blobPath, 0L, "", "", System.currentTimeMillis(), ""));
    }
    
    @Override
    public void save(BlobMetadata metadata) {
        String key = METADATA_KEY_PREFIX + metadata.path();
        // Simple serialization - in production use proper JSON library
        String value = String.valueOf(metadata.size());
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(DEFAULT_TTL));
        log.debug("Cached blob metadata for: {}", metadata.path());
    }
    
    @Override
    public void delete(String blobPath) {
        String key = METADATA_KEY_PREFIX + blobPath;
        redisTemplate.delete(key);
        log.debug("Deleted cached metadata for: {}", blobPath);
    }
}

