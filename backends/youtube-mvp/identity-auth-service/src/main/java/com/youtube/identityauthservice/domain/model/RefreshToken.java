package com.youtube.identityauthservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Domain entity representing a refresh token.
 * Contains token information and rotation tracking.
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
public class RefreshToken {

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
}
