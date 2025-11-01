package com.youtube.common.domain.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for transactional outbox pattern entities.
 * 
 * <p>This abstract entity implements the transactional outbox pattern, ensuring reliable
 * event publishing by storing domain events in the database within the same transaction
 * as the business logic that generates them.</p>
 * 
 * <p>Services should extend this class and configure it with their schema-specific
 * table mapping:</p>
 * 
 * <pre>{@code
 * @Entity
 * @Table(name = "outbox_events", schema = "your_schema",
 *         indexes = @Index(name = "ix_outbox_not_dispatched", columnList = "created_at"))
 * public class YourOutboxEvent extends OutboxEvent {
 * }
 * }</pre>
 * 
 * <p>The outbox pattern ensures:</p>
 * <ul>
 *   <li>Events are stored atomically with domain changes</li>
 *   <li>Event publishing is eventually consistent</li>
 *   <li>No events are lost even if the message broker is temporarily unavailable</li>
 * </ul>
 * 
 * @see <a href="https://microservices.io/patterns/data/transactional-outbox.html">Transactional Outbox Pattern</a>
 */
@MappedSuperclass
@Getter
@Setter
public abstract class OutboxEvent {

    /**
     * Unique identifier for the outbox event (typically ULID).
     */
    @Id
    @Column(name = "id", length = 26, nullable = false, updatable = false)
    private String id;

    /**
     * Type of the domain event (e.g., "user_created", "order_placed").
     */
    @Column(name = "event_type", nullable = false, length = 200, updatable = false)
    private String eventType;

    /**
     * Type of the aggregate root that generated this event (e.g., "User", "Order").
     */
    @Column(name = "aggregate_type", length = 100, updatable = false)
    private String aggregateType;

    /**
     * Identifier of the aggregate root that generated this event.
     */
    @Column(name = "aggregate_id", length = 128, updatable = false)
    private String aggregateId;

    /**
     * JSON payload containing the event data.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, updatable = false)
    private String payloadJson;

    /**
     * Correlation ID for tracing requests across services.
     */
    @Column(name = "correlation_id", length = 64, updatable = false)
    private String correlationId;

    /**
     * Causation ID linking this event to the event that caused it.
     */
    @Column(name = "causation_id", length = 64, updatable = false)
    private String causationId;

    /**
     * W3C traceparent header for distributed tracing.
     */
    @Column(name = "traceparent", length = 255, updatable = false)
    private String traceparent;

    /**
     * Timestamp when the event was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the event was successfully dispatched to the message broker.
     * Null if not yet dispatched.
     */
    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    /**
     * Message ID assigned by the message broker (Service Bus, Kafka, etc.).
     */
    @Column(name = "broker_message_id", length = 200)
    private String brokerMessageId;

    /**
     * Error message if dispatch failed.
     */
    @Column(name = "error", length = 4000)
    private String error;

    /**
     * Sets the created timestamp before persisting if not already set.
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Returns the event type (alias for {@link #getEventType()}).
     * 
     * @return the event type
     */
    public String getType() {
        return eventType;
    }

    /**
     * Returns the payload JSON (alias for {@link #getPayloadJson()}).
     * 
     * @return the payload JSON string
     */
    public String getPayload() {
        return payloadJson;
    }

    /**
     * Returns the occurred timestamp (alias for {@link #getCreatedAt()}).
     * 
     * @return when the event occurred
     */
    public Instant getOccurredAt() {
        return createdAt;
    }

    /**
     * Returns the partition key for message broker partitioning.
     * 
     * <p>Defaults to the aggregate ID to ensure events for the same aggregate
     * are processed in order.</p>
     * 
     * @return the partition key, or null if aggregate ID is not set
     */
    public String getPartitionKey() {
        return aggregateId;
    }

    /**
     * Checks if this event has been dispatched.
     * 
     * @return true if dispatchedAt is not null, false otherwise
     */
    public boolean isDispatched() {
        return dispatchedAt != null;
    }

    /**
     * Checks if this event has failed to dispatch.
     * 
     * @return true if error is not null and not empty, false otherwise
     */
    public boolean hasError() {
        return error != null && !error.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutboxEvent that = (OutboxEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OutboxEvent{" +
                "id='" + id + '\'' +
                ", eventType='" + eventType + '\'' +
                ", aggregateType='" + aggregateType + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", createdAt=" + createdAt +
                ", dispatchedAt=" + dispatchedAt +
                ", hasError=" + hasError() +
                '}';
    }
}

