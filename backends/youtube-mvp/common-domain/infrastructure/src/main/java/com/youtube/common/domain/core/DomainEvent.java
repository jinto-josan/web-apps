package com.youtube.common.domain.core;

import com.github.f4b6a3.ulid.UlidCreator;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for all domain events.
 * Domain events represent something that happened in the domain.
 * 
 * <p>All domain events should:</p>
 * <ul>
 *   <li>Be immutable</li>
 *   <li>Have a unique event ID</li>
 *   <li>Record when they occurred</li>
 * </ul>
 */
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
    
    public String getEventId() {
        return eventId;
    }
    
    public Instant getOccurredAt() {
        return occurredAt;
    }
    
    /**
     * Returns the event type name for serialization and routing.
     * 
     * @return the event type name
     */
    public abstract String getEventType();
}

