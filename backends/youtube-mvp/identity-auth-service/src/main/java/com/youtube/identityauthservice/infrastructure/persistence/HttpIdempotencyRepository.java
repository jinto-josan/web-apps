package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.model.HttpIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HttpIdempotencyRepository extends JpaRepository<HttpIdempotency, Long> {
    Optional<HttpIdempotency> findByIdempotencyKeyAndRequestHash(String idempotencyKey, byte[] requestHash);
}
