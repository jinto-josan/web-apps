package com.youtube.identityauthservice.domain.events;

import com.youtube.common.domain.core.DomainEvent;
import com.youtube.identityauthservice.domain.entities.UserType;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a new user is created.
 * Supports both regular users (with email) and service principals (with servicePrincipalId).
 */
@Getter
public final class UserCreated extends DomainEvent {
    
    private final String userId;
    private final UserType userType;
    private final String email;
    private final String normalizedEmail;
    private final String servicePrincipalId;
    private final String displayName;
    private final boolean emailVerified;
    private final short status;
    private final Instant createdAt;
    
    public UserCreated(String userId, String email, String normalizedEmail, String displayName,
                      boolean emailVerified, short status, Instant createdAt) {
        super();
        this.userId = Objects.requireNonNull(userId);
        this.userType = UserType.USER;
        this.email = email;
        this.normalizedEmail = normalizedEmail;
        this.servicePrincipalId = null;
        this.displayName = Objects.requireNonNull(displayName);
        this.emailVerified = emailVerified;
        this.status = status;
        this.createdAt = Objects.requireNonNull(createdAt);
    }
    
    public UserCreated(String userId, UserType userType, String email, String normalizedEmail,
                      String servicePrincipalId, String displayName, boolean emailVerified,
                      short status, Instant createdAt) {
        super();
        this.userId = Objects.requireNonNull(userId);
        this.userType = Objects.requireNonNull(userType);
        this.email = email;
        this.normalizedEmail = normalizedEmail;
        this.servicePrincipalId = servicePrincipalId;
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
        this.userType = UserType.USER;
        this.email = email;
        this.normalizedEmail = normalizedEmail;
        this.servicePrincipalId = null;
        this.displayName = Objects.requireNonNull(displayName);
        this.emailVerified = emailVerified;
        this.status = status;
        this.createdAt = Objects.requireNonNull(createdAt);
    }
    
    public UserCreated(String eventId, Instant occurredAt, String userId, UserType userType,
                      String email, String normalizedEmail, String servicePrincipalId,
                      String displayName, boolean emailVerified, short status, Instant createdAt) {
        super(eventId, occurredAt);
        this.userId = Objects.requireNonNull(userId);
        this.userType = Objects.requireNonNull(userType);
        this.email = email;
        this.normalizedEmail = normalizedEmail;
        this.servicePrincipalId = servicePrincipalId;
        this.displayName = Objects.requireNonNull(displayName);
        this.emailVerified = emailVerified;
        this.status = status;
        this.createdAt = Objects.requireNonNull(createdAt);
    }
    
    @Override
    public String getEventType() {
        return "user.created";
    }
}

