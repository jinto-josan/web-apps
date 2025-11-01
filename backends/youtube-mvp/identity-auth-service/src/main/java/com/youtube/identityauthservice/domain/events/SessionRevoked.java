package com.youtube.identityauthservice.domain.events;

import com.youtube.common.domain.core.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a session is revoked (logout).
 */
@Getter
public final class SessionRevoked extends DomainEvent {
    
    private final String sessionId;
    private final String userId;
    private final String reason;
    
    public SessionRevoked(String sessionId, String userId, String reason) {
        super();
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.reason = reason;
    }
    
    public SessionRevoked(String eventId, Instant occurredAt, String sessionId, String userId, String reason) {
        super(eventId, occurredAt);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.reason = reason;
    }
}

