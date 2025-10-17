package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity.
 * Defines operations for refresh token persistence.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(byte[] tokenHash);
    List<RefreshToken> findBySessionId(String sessionId);
}
