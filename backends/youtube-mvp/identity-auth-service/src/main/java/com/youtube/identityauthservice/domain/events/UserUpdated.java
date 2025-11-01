package com.youtube.identityauthservice.domain.events;

import com.youtube.common.domain.core.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a user is updated.
 */
@Getter
public final class UserUpdated extends DomainEvent {
    
    private final String userId;
    private final String email;
    private final String displayName;
    private final boolean emailVerified;
    
    public UserUpdated(String userId, String email, String displayName, boolean emailVerified) {
        super();
        this.userId = Objects.requireNonNull(userId);
        this.email = Objects.requireNonNull(email);
        this.displayName = Objects.requireNonNull(displayName);
        this.emailVerified = emailVerified;
    }
    
    public UserUpdated(String eventId, Instant occurredAt, String userId, String email,
                      String displayName, boolean emailVerified) {
        super(eventId, occurredAt);
        this.userId = Objects.requireNonNull(userId);
        this.email = Objects.requireNonNull(email);
        this.displayName = Objects.requireNonNull(displayName);
        this.emailVerified = emailVerified;
    }
}

