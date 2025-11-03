package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.infrastructure.persistence.entity.HttpIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for HttpIdempotency entities in identity-auth-service.
 * 
 * <p>This is a Spring Data JPA repository used by the JPA adapter
 * configured in IdempotencyConfig.</p>
 */
@Repository
public interface HttpIdempotencyJpaRepository extends JpaRepository<HttpIdempotencyEntity, Long> {
    Optional<HttpIdempotencyEntity> findByIdempotencyKeyAndRequestHash(String idempotencyKey, byte[] requestHash);
}
