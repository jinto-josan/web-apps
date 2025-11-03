package com.youtube.common.domain.persistence.idempotency.redis;

import com.youtube.common.domain.persistence.idempotency.HttpIdempotencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * Redis-based implementation of HttpIdempotencyRepository.
 * 
 * <p>This implementation is auto-configured when StringRedisTemplate is available
 * and no JPA adapter is present.</p>
 * 
 * <p>Stores idempotency records in Redis with a TTL of 24 hours.
 * Redis key format: "idempotency:{key}:{base64(hash)}"
 * Value format: JSON with status and base64-encoded body.</p>
 */
@Repository
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnMissingBean(com.youtube.common.domain.persistence.idempotency.jpa.JpaHttpIdempotencyRepository.JpaIdempotencyRepositoryAdapter.class)
public class RedisHttpIdempotencyRepository implements HttpIdempotencyRepository {
    
    private static final Logger log = LoggerFactory.getLogger(RedisHttpIdempotencyRepository.class);
    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    public RedisHttpIdempotencyRepository(
            StringRedisTemplate redisTemplate,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        log.info("RedisHttpIdempotencyRepository initialized");
    }
    
    @Override
    public Optional<StoredResponse> findByIdempotencyKeyAndRequestHash(String key, byte[] hash) {
        String redisKey = buildRedisKey(key, hash);
        String json = redisTemplate.opsForValue().get(redisKey);
        
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            return parseStoredResponse(json);
        } catch (Exception e) {
            log.warn("Failed to parse stored response for key {}", key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public void storeResponse(String key, byte[] requestHash, int status, byte[] body) {
        String redisKey = buildRedisKey(key, requestHash);
        
        try {
            String json = serializeResponse(status, body);
            redisTemplate.opsForValue().set(redisKey, json, TTL);
        } catch (Exception e) {
            log.error("Failed to store idempotency response for key {}", key, e);
            throw new RuntimeException("Failed to store idempotency response", e);
        }
    }
    
    private String buildRedisKey(String idempotencyKey, byte[] hash) {
        String hashBase64 = Base64.getEncoder().encodeToString(hash);
        return KEY_PREFIX + idempotencyKey + ":" + hashBase64;
    }
    
    private String serializeResponse(int status, byte[] body) throws Exception {
        String bodyBase64 = body != null ? Base64.getEncoder().encodeToString(body) : null;
        ResponseData data = new ResponseData(status, bodyBase64);
        return objectMapper.writeValueAsString(data);
    }
    
    private Optional<StoredResponse> parseStoredResponse(String json) throws Exception {
        ResponseData data = objectMapper.readValue(json, ResponseData.class);
        byte[] body = data.body() != null ? Base64.getDecoder().decode(data.body()) : null;
        return Optional.of(new StoredResponse(data.status(), body));
    }
    
    /**
     * Internal data class for JSON serialization.
     */
    private record ResponseData(int status, String body) {}
}

