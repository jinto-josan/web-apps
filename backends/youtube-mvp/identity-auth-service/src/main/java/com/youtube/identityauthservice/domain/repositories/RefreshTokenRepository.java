package com.youtube.identityauthservice.domain.repositories;

import com.youtube.identityauthservice.domain.entities.RefreshToken;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for refresh token entities.
 */
public interface RefreshTokenRepository {
    
    Optional<RefreshToken> findByTokenHash(byte[] tokenHash);
    
    RefreshToken save(RefreshToken refreshToken);
    
    List<RefreshToken> findBySessionId(String sessionId);
}

