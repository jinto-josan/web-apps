package com.youtube.common.domain.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.common.domain.DomainEvent;
import com.youtube.common.domain.shared.valueobjects.UserId;

import java.time.Instant;

/**
 * Domain event published when a new user is created.
 * 
 * <p>Published by: identity-auth-service
 * <p>Consumed by:
 * <ul>
 *   <li>user-profile-service - Create user profile</li>
 *   <li>notifications-service - Send welcome notification</li>
 *   <li>recommendations-service - Initialize user features</li>
 * </ul>
 */
public final class UserCreatedEvent extends DomainEvent {
    
    private final UserId userId;
    private final String email;
    private final String username;

    @JsonCreator
    public UserCreatedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("userId") UserId userId,
            @JsonProperty("email") String email,
            @JsonProperty("username") String username) {
        super(eventId, occurredAt);
        this.userId = userId;
        this.email = email;
        this.username = username;
    }

    public UserCreatedEvent(UserId userId, String email, String username) {
        this.userId = userId;
        this.email = email;
        this.username = username;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getEventType() {
        return "user.created";
    }
}

