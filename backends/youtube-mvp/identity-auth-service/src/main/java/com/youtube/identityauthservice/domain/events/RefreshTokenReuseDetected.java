package com.youtube.identityauthservice.domain.events;

import com.youtube.common.domain.core.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when refresh token reuse is detected (security event).
 */
@Getter
public final class RefreshTokenReuseDetected extends DomainEvent {
    
    private final String sessionId;
    private final String userId;
    private final String tokenId;
    private final String reason;
    
    public RefreshTokenReuseDetected(String sessionId, String userId, String tokenId, String reason) {
        super();
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.tokenId = tokenId;
        this.reason = Objects.requireNonNull(reason);
    }
    
    public RefreshTokenReuseDetected(String eventId, Instant occurredAt, String sessionId, String userId,
                                     String tokenId, String reason) {
        super(eventId, occurredAt);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.tokenId = tokenId;
        this.reason = Objects.requireNonNull(reason);
    }
    
    @Override
    public String getEventType() {
        return "refresh_token.reuse_detected";
    }
}

