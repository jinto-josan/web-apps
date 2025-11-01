package com.youtube.identityauthservice.domain.events;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a new user is created.
 */
@Getter
public final class UserCreated extends DomainEvent {
    
    private final String userId;
    private final String email;
    private final String normalizedEmail;
    private final String displayName;
    private final boolean emailVerified;
    private final short status;
    private final Instant createdAt;
    
    public UserCreated(String userId, String email, String normalizedEmail, String displayName,
                      boolean emailVerified, short status, Instant createdAt) {
        super();
        this.userId = Objects.requireNonNull(userId);
        this.email = Objects.requireNonNull(email);
        this.normalizedEmail = Objects.requireNonNull(normalizedEmail);
        this.displayName = Objects.requireNonNull(displayName);
        this.emailVerified = emailVerified;
        this.status = status;
        this.createdAt = Objects.requireNonNull(createdAt);
    }
    
    public UserCreated(String eventId, Instant occurredAt, String userId, String email, 
                      String normalizedEmail, String displayName, boolean emailVerified, 
                      short status, Instant createdAt) {
        super(eventId, occurredAt);
        this.userId = Objects.requireNonNull(userId);
        this.email = Objects.requireNonNull(email);
        this.normalizedEmail = Objects.requireNonNull(normalizedEmail);
        this.displayName = Objects.requireNonNull(displayName);
        this.emailVerified = emailVerified;
        this.status = status;
        this.createdAt = Objects.requireNonNull(createdAt);
    }
}

