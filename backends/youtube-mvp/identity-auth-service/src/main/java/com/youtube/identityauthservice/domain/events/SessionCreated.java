package com.youtube.identityauthservice.domain.events;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a new session is created.
 */
@Getter
public final class SessionCreated extends DomainEvent {
    
    private final String sessionId;
    private final String userId;
    private final String deviceId;
    private final String userAgent;
    private final String ip;
    
    public SessionCreated(String sessionId, String userId, String deviceId, String userAgent, String ip) {
        super();
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.deviceId = deviceId;
        this.userAgent = userAgent;
        this.ip = ip;
    }
    
    public SessionCreated(String eventId, Instant occurredAt, String sessionId, String userId,
                         String deviceId, String userAgent, String ip) {
        super(eventId, occurredAt);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.userId = Objects.requireNonNull(userId);
        this.deviceId = deviceId;
        this.userAgent = userAgent;
        this.ip = ip;
    }
}

