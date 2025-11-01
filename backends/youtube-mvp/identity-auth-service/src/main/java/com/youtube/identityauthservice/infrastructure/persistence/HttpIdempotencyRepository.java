package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.infrastructure.persistence.entity.HttpIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HttpIdempotencyRepository extends JpaRepository<HttpIdempotencyEntity, Long> {
    Optional<HttpIdempotencyEntity> findByIdempotencyKeyAndRequestHash(String idempotencyKey, byte[] requestHash);
}
