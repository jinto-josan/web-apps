package com.youtube.userprofileservice.infrastructure.persistence.repository;

import com.youtube.userprofileservice.infrastructure.persistence.entity.HttpIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for HttpIdempotency entities.
 */
@Repository
public interface HttpIdempotencyJpaRepository extends JpaRepository<HttpIdempotencyEntity, Long> {
    Optional<HttpIdempotencyEntity> findByIdempotencyKeyAndRequestHash(String idempotencyKey, byte[] requestHash);
}

