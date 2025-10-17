package com.youtube.common.domain;

import java.util.Objects;

/**
 * Wraps a domain event with its metadata and serialized payload.
 * Used for event publishing and processing in distributed systems.
 */
public class EventEnvelope {
    private final DomainEvent event;
    private final EventMetadata metadata;
    private final String payloadJson;

    public EventEnvelope(DomainEvent event, EventMetadata metadata, String payloadJson) {
        this.event = Objects.requireNonNull(event, "Event cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
        this.payloadJson = Objects.requireNonNull(payloadJson, "Payload JSON cannot be null");
    }

    public DomainEvent getEvent() {
        return event;
    }

    public EventMetadata getMetadata() {
        return metadata;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventEnvelope that = (EventEnvelope) o;
        return Objects.equals(event, that.event) &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(payloadJson, that.payloadJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, metadata, payloadJson);
    }

    @Override
    public String toString() {
        return "EventEnvelope{event=" + event + ", metadata=" + metadata + "}";
    }
}
