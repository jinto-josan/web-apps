package com.youtube.identityauthservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "outbox_events", schema = "auth",
        indexes = { @Index(name = "ix_auth_outbox_not_dispatched", columnList = "created_at") }
)
@Getter
@Setter
public class OutboxEvent {

    @Id
    @Column(length = 26, nullable = false)
    private String id;

    @Column(name = "event_type", nullable = false, length = 200)
    private String eventType;

    @Column(name = "aggregate_type", length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 128)
    private String aggregateId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    private String payloadJson;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "causation_id", length = 64)
    private String causationId;

    @Column(name = "traceparent", length = 255)
    private String traceparent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "broker_message_id", length = 200)
    private String brokerMessageId;

    @Column(name = "error", length = 4000)
    private String error;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
    public String getType() { return this.eventType; }

    public String getPayload() { return this.payloadJson; }

    public Instant getOccurredAt() { return this.createdAt; }

    // Optional: expose a partition key; Service Bus can benefit from this
    public String getPartitionKey() { return this.aggregateId; }
}