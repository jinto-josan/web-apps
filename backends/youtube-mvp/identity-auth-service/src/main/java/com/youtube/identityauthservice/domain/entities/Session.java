package com.youtube.identityauthservice.domain.entities;

import com.youtube.common.domain.core.Entity;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.domain.valueobjects.SessionId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain entity representing a user session.
 * Extends Entity from common-domain for identity management.
 */
@Getter
public class Session extends Entity<SessionId> {
    
    private UserId userId;
    private String jti;
    private String deviceId;
    private String userAgent;
    private String ip;
    private Instant revokedAt;
    private String revokeReason;
    
    protected Session() {
        // For JPA and framework deserialization
        super();
    }
    
    @Builder
    protected Session(
            SessionId id,
            UserId userId,
            String jti,
            String deviceId,
            String userAgent,
            String ip,
            Instant revokedAt,
            String revokeReason) {
        super(id);
        this.userId = userId;
        this.jti = jti;
        this.deviceId = deviceId;
        this.userAgent = userAgent;
        this.ip = ip;
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
    }
    
    public Session revoke(String reason) {
        return new Session(
            getId(), userId, jti, deviceId, userAgent, ip,
            Instant.now(), reason
        );
    }
}

