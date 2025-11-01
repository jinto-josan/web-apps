package com.youtube.identityauthservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity for RefreshToken.
 * Maps domain RefreshToken entity to database table.
 */
@Entity
@Table(name = "refresh_tokens", schema = "auth",
        indexes = {
                @Index(name = "ix_auth_rt_session", columnList = "session_id, created_at DESC"),
                @Index(name = "ix_auth_rt_expires", columnList = "expires_at"),
                @Index(name = "ix_auth_rt_revoked", columnList = "revoked_at")
        }
)
@Getter
@Setter
public class RefreshTokenEntity {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @Column(name = "session_id", nullable = false, length = 26)
    private String sessionId;

    @Column(name = "token_hash", nullable = false, columnDefinition = "bytea")
    private byte[] tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "replaced_by_token_id", length = 26)
    private String replacedByTokenId;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason", length = 200)
    private String revokeReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    
    /**
     * Converts JPA entity to domain entity.
     */
    public com.youtube.identityauthservice.domain.entities.RefreshToken toDomain() {
        return com.youtube.identityauthservice.domain.entities.RefreshToken.builder()
                .id(id)
                .sessionId(sessionId)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .replacedByTokenId(replacedByTokenId)
                .revokedAt(revokedAt)
                .revokeReason(revokeReason)
                .createdAt(createdAt)
                .build();
    }
    
    /**
     * Creates JPA entity from domain entity.
     */
    public static RefreshTokenEntity fromDomain(com.youtube.identityauthservice.domain.entities.RefreshToken refreshToken) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(refreshToken.getId());
        entity.setSessionId(refreshToken.getSessionId());
        entity.setTokenHash(refreshToken.getTokenHash());
        entity.setExpiresAt(refreshToken.getExpiresAt());
        entity.setReplacedByTokenId(refreshToken.getReplacedByTokenId());
        entity.setRevokedAt(refreshToken.getRevokedAt());
        entity.setRevokeReason(refreshToken.getRevokeReason());
        entity.setCreatedAt(refreshToken.getCreatedAt());
        return entity;
    }
}

