package com.youtube.identityauthservice.domain.entities;

import com.youtube.common.domain.core.Entity;
import com.youtube.identityauthservice.domain.valueobjects.RefreshTokenId;
import com.youtube.identityauthservice.domain.valueobjects.SessionId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain entity representing a refresh token.
 * Extends Entity from common-domain for identity management.
 */
@Getter
public class RefreshToken extends Entity<RefreshTokenId> {
    
    private SessionId sessionId;
    private byte[] tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokeReason;
    private RefreshTokenId replacedByTokenId;
    
    protected RefreshToken() {
        // For JPA and framework deserialization
        super();
    }
    
    @Builder
    protected RefreshToken(
            RefreshTokenId id,
            SessionId sessionId,
            byte[] tokenHash,
            Instant createdAt,
            Instant expiresAt,
            Instant revokedAt,
            String revokeReason,
            RefreshTokenId replacedByTokenId) {
        super(id);
        this.sessionId = sessionId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
        this.replacedByTokenId = replacedByTokenId;
    }
    
    public RefreshToken revoke(String reason) {
        return new RefreshToken(
            getId(), sessionId, tokenHash, createdAt, expiresAt,
            Instant.now(), reason, replacedByTokenId
        );
    }
    
    public RefreshToken replaceWith(RefreshTokenId newTokenId) {
        return new RefreshToken(
            getId(), sessionId, tokenHash, createdAt, expiresAt,
            revokedAt, revokeReason, newTokenId
        );
    }
}

