package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.entities.RefreshToken;
import com.youtube.identityauthservice.domain.repositories.RefreshTokenRepository;
import com.youtube.identityauthservice.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of domain RefreshTokenRepository using JPA.
 */
@Component
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepo;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(byte[] tokenHash) {
        return jpaRepo.findByTokenHash(tokenHash)
                .map(RefreshTokenEntity::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = jpaRepo.findById(refreshToken.getId())
                .map(existing -> {
                    existing.setSessionId(refreshToken.getSessionId());
                    existing.setTokenHash(refreshToken.getTokenHash());
                    existing.setExpiresAt(refreshToken.getExpiresAt());
                    existing.setReplacedByTokenId(refreshToken.getReplacedByTokenId());
                    existing.setRevokedAt(refreshToken.getRevokedAt());
                    existing.setRevokeReason(refreshToken.getRevokeReason());
                    existing.setCreatedAt(refreshToken.getCreatedAt());
                    return existing;
                })
                .orElseGet(() -> RefreshTokenEntity.fromDomain(refreshToken));
        RefreshTokenEntity saved = jpaRepo.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<RefreshToken> findBySessionId(String sessionId) {
        return jpaRepo.findBySessionId(sessionId).stream()
                .map(RefreshTokenEntity::toDomain)
                .collect(Collectors.toList());
    }
}

