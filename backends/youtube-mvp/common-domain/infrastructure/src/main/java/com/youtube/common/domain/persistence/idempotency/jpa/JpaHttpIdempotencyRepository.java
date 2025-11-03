package com.youtube.common.domain.persistence.idempotency.jpa;

import com.youtube.common.domain.persistence.entity.HttpIdempotency;
import com.youtube.common.domain.persistence.idempotency.HttpIdempotencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-based implementation of HttpIdempotencyRepository.
 * 
 * <p>This implementation is auto-configured when a JpaIdempotencyRepositoryAdapter
 * bean is available in the Spring context.</p>
 * 
 * <p>Services using JPA should create an adapter bean like:</p>
 * <pre>{@code
 * @Bean
 * public JpaIdempotencyRepositoryAdapter httpIdempotencyAdapter(
 *         HttpIdempotencyJpaRepository jpaRepository) {
 *     return new JpaIdempotencyRepositoryAdapter(
 *         jpaRepository::findByIdempotencyKeyAndRequestHash,
 *         jpaRepository::save,
 *         HttpIdempotencyEntity::new
 *     );
 * }
 * }</pre>
 */
@Repository
@ConditionalOnBean(JpaHttpIdempotencyRepository.JpaIdempotencyRepositoryAdapter.class)
public class JpaHttpIdempotencyRepository implements HttpIdempotencyRepository {
    
    private static final Logger log = LoggerFactory.getLogger(JpaHttpIdempotencyRepository.class);
    
    private final JpaIdempotencyRepositoryAdapter adapter;
    
    public JpaHttpIdempotencyRepository(ObjectProvider<JpaIdempotencyRepositoryAdapter> adapterProvider) {
        this.adapter = adapterProvider.getObject();
        log.info("JpaHttpIdempotencyRepository initialized with JPA adapter");
    }
    
    @Override
    public Optional<StoredResponse> findByIdempotencyKeyAndRequestHash(String key, byte[] hash) {
        Optional<HttpIdempotency> entityOpt = adapter.find(key, hash);
        
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }
        
        HttpIdempotency entity = entityOpt.get();
        
        // Validate request hash matches
        if (!entity.matchesRequestHash(hash)) {
            log.warn("Idempotency key {} reused with different request hash", key);
            return Optional.empty();
        }
        
        // Check if response is available
        if (!entity.hasResponse()) {
            return Optional.empty();
        }
        
        return Optional.of(new HttpIdempotencyRepository.StoredResponse(
            entity.getResponseStatus(),
            entity.getResponseBody()
        ));
    }
    
    @Override
    public void storeResponse(String key, byte[] requestHash, int status, byte[] body) {
        // Find existing or create new
        Optional<HttpIdempotency> existingOpt = adapter.find(key, requestHash);
        
        HttpIdempotency entity;
        if (existingOpt.isPresent()) {
            entity = existingOpt.get();
            // Validate hash matches
            if (!entity.matchesRequestHash(requestHash)) {
                log.warn("Attempting to store response for key {} with different hash", key);
                // Create new entity instead
                entity = adapter.create();
                entity.setIdempotencyKey(key);
                entity.setRequestHash(requestHash);
            }
        } else {
            entity = adapter.create();
            entity.setIdempotencyKey(key);
            entity.setRequestHash(requestHash);
        }
        
        entity.storeResponse(status, body);
        adapter.save(entity);
    }
    
    /**
     * Adapter interface for JPA repository operations.
     * Services should create a bean implementing this adapter.
     */
    public interface JpaIdempotencyRepositoryAdapter {
        /**
         * Finds an entity by idempotency key and request hash.
         */
        Optional<HttpIdempotency> find(String key, byte[] hash);
        
        /**
         * Saves an entity.
         */
        HttpIdempotency save(HttpIdempotency entity);
        
        /**
         * Creates a new entity instance.
         */
        HttpIdempotency create();
    }
}

