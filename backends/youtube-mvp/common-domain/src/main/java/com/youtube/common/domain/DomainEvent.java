package com.youtube.common.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for domain events.
 * Domain events represent something important that happened in the domain.
 */
public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    protected DomainEvent(String eventId, Instant occurredAt) {
        this.eventId = Objects.requireNonNull(eventId, "Event ID cannot be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
    }

    /**
     * Gets the unique identifier of this event.
     * 
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the timestamp when this event occurred.
     * 
     * @return the occurrence timestamp
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * Gets the type of this event.
     * 
     * @return the event type
     */
    public String getEventType() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEvent that = (DomainEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{eventId='" + eventId + "', occurredAt=" + occurredAt + "}";
    }
}
