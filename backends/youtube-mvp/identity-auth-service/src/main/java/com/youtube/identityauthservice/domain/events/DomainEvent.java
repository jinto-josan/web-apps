package com.youtube.identityauthservice.domain.events;

import com.github.f4b6a3.ulid.UlidCreator;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for all domain events.
 * Follows Domain-Driven Design patterns.
 */
@Getter
public abstract class DomainEvent {
    
    private final String eventId;
    private final Instant occurredAt;
    
    protected DomainEvent() {
        this.eventId = UlidCreator.getUlid().toString();
        this.occurredAt = Instant.now();
    }
    
    protected DomainEvent(String eventId, Instant occurredAt) {
        this.eventId = Objects.requireNonNull(eventId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }
}

