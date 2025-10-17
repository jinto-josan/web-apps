package com.youtube.identityauthservice.domain.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Domain entity representing a user session.
 * Contains session information and tracking data.
 */
@Entity
@Table(name = "sessions", schema = "auth",
        indexes = {
                @Index(name = "ix_auth_sessions_user_created", columnList = "user_id, created_at DESC"),
                @Index(name = "ix_auth_sessions_revoked_at", columnList = "revoked_at")
        }
)
@Getter
@Setter
public class Session {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(nullable = false, length = 64)
    private String jti;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(length = 45)
    private String ip;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason", length = 200)
    private String revokeReason;

    @Column(name = "mfa_verified_at")
    private Instant mfaVerifiedAt;

    @Version
    @Column(nullable = false)
    private Integer version = 0;
}
