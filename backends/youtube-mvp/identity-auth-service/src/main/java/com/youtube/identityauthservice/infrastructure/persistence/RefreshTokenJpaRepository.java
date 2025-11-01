package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository interface for RefreshTokenEntity.
 * Defines operations for refresh token persistence.
 */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByTokenHash(byte[] tokenHash);
    List<RefreshTokenEntity> findBySessionId(String sessionId);
}
