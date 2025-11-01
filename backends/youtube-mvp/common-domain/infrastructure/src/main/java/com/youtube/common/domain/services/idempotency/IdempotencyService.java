package com.youtube.common.domain.services.idempotency;

import com.youtube.common.domain.persistence.entity.HttpIdempotency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;

/**
 * Service for handling HTTP request idempotency.
 * Uses Redis for distributed locking and SQL for result storage.
 */
@Service
public class IdempotencyService {
    
    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    
    private static final String LOCK_PREFIX = "idempotency:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(60);
    
    private final IdempotencyRepository idempotencyRepository;
    private final StringRedisTemplate redisTemplate;
    
    public IdempotencyService(
        IdempotencyRepository idempotencyRepository,
        StringRedisTemplate redisTemplate
    ) {
        this.idempotencyRepository = idempotencyRepository;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Gets a stored result for an idempotency key, if it exists.
     * 
     * @param idempotencyKey the idempotency key
     * @param requestHash the hash of the request (method + URI + body)
     * @return the stored response, or empty if not found or hash mismatch
     */
    public Optional<StoredResponse> getStoredResult(String idempotencyKey, byte[] requestHash) {
        Optional<HttpIdempotency> record = idempotencyRepository.findByIdempotencyKeyAndRequestHash(
            idempotencyKey, requestHash
        );
        
        if (record.isEmpty()) {
            return Optional.empty();
        }
        
        HttpIdempotency idempotency = record.get();
        
        // Check if request hash matches
        if (!idempotency.matchesRequestHash(requestHash)) {
            log.warn("Idempotency key {} reused with different request", idempotencyKey);
            return Optional.empty();
        }
        
        // Check if we have a stored response
        if (!idempotency.hasResponse()) {
            // Request is in progress but not yet completed
            return Optional.empty();
        }
        
        return Optional.of(new StoredResponse(
            idempotency.getResponseStatus(),
            idempotency.getResponseBody()
        ));
    }
    
    /**
     * Attempts to acquire a lock for processing an idempotent request.
     * 
     * @param idempotencyKey the idempotency key
     * @return true if lock was acquired, false if already locked
     */
    public boolean acquireLock(String idempotencyKey) {
        String lockKey = LOCK_PREFIX + idempotencyKey;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }
    
    /**
     * Releases the lock for an idempotency key.
     * 
     * @param idempotencyKey the idempotency key
     */
    public void releaseLock(String idempotencyKey) {
        String lockKey = LOCK_PREFIX + idempotencyKey;
        redisTemplate.delete(lockKey);
    }
    
    /**
     * Stores the result of an idempotent request.
     * 
     * @param idempotencyKey the idempotency key
     * @param requestHash the hash of the request
     * @param responseStatus the HTTP response status
     * @param responseBody the response body
     */
    public void storeResult(String idempotencyKey, byte[] requestHash, int responseStatus, byte[] responseBody) {
        idempotencyRepository.upsert(
            idempotencyKey,
            requestHash,
            responseStatus,
            responseBody
        );
        
        // Release lock after storing result
        releaseLock(idempotencyKey);
    }
    
    /**
     * Computes the hash of a request for idempotency checking.
     * 
     * @param method the HTTP method
     * @param uri the request URI
     * @param body the request body, or null
     * @return the SHA-256 hash of the request
     */
    public static byte[] computeRequestHash(String method, String uri, byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(method.getBytes(StandardCharsets.UTF_8));
            digest.update(uri.getBytes(StandardCharsets.UTF_8));
            if (body != null) {
                digest.update(body);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Record for storing response data.
     */
    public record StoredResponse(int status, byte[] body) {}
    
    /**
     * Repository interface for idempotency records.
     */
    public interface IdempotencyRepository {
        Optional<HttpIdempotency> findByIdempotencyKeyAndRequestHash(String key, byte[] hash);
        void upsert(String key, byte[] requestHash, int status, byte[] body);
    }
}

