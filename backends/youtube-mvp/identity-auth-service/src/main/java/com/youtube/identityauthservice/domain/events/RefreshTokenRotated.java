package com.youtube.identityauthservice.domain.events;

import com.youtube.common.domain.core.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a refresh token is rotated.
 */
@Getter
public final class RefreshTokenRotated extends DomainEvent {
    
    private final String sessionId;
    private final String userId;
    private final String oldTokenId;
    private final String newTokenId;
    
    public RefreshTokenRotated(String sessionId, String userId, String oldTokenId, String newTokenId) {
        super();
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.oldTokenId = oldTokenId;
        this.newTokenId = Objects.requireNonNull(newTokenId);
    }
    
    public RefreshTokenRotated(String eventId, Instant occurredAt, String sessionId, String userId,
                               String oldTokenId, String newTokenId) {
        super(eventId, occurredAt);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.oldTokenId = oldTokenId;
        this.newTokenId = Objects.requireNonNull(newTokenId);
    }
    
    @Override
    public String getEventType() {
        return "refresh_token.rotated";
    }
}

