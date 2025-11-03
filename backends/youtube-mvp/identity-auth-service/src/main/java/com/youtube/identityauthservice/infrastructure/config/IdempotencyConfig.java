package com.youtube.identityauthservice.infrastructure.config;

import com.youtube.common.domain.persistence.entity.HttpIdempotency;
import com.youtube.common.domain.persistence.idempotency.jpa.JpaHttpIdempotencyRepository;
import com.youtube.identityauthservice.infrastructure.persistence.HttpIdempotencyJpaRepository;
import com.youtube.identityauthservice.infrastructure.persistence.entity.HttpIdempotencyEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Configuration for HTTP idempotency using JPA.
 * 
 * <p>Creates an adapter bean that connects the service-specific JPA repository
 * to the common-domain JpaHttpIdempotencyRepository.</p>
 */
@Configuration
public class IdempotencyConfig {
    
    /**
     * Creates the JPA adapter bean for idempotency repository.
     * This enables auto-configuration of JpaHttpIdempotencyRepository in common-domain.
     */
    @Bean
    public JpaHttpIdempotencyRepository.JpaIdempotencyRepositoryAdapter httpIdempotencyAdapter(
            HttpIdempotencyJpaRepository jpaRepository) {
        return new JpaHttpIdempotencyRepository.JpaIdempotencyRepositoryAdapter() {
            @Override
            public Optional<HttpIdempotency> find(String key, byte[] hash) {
                return jpaRepository.findByIdempotencyKeyAndRequestHash(key, hash)
                    .map(entity -> (HttpIdempotency) entity);
            }
            
            @Override
            public HttpIdempotency save(HttpIdempotency entity) {
                HttpIdempotencyEntity saved = jpaRepository.save((HttpIdempotencyEntity) entity);
                return saved;
            }
            
            @Override
            public HttpIdempotency create() {
                return new HttpIdempotencyEntity();
            }
        };
    }
}

