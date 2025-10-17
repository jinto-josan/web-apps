package com.youtube.common.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a message in the outbox pattern for reliable event publishing.
 * Ensures that domain events are eventually published even if the initial attempt fails.
 */
public class OutboxMessage {
    private final String id;
    private final String aggregateType;
    private final String aggregateId;
    private final String eventType;
    private final int version;
    private final String payloadJson;
    private final Map<String, String> headers;
    private final Instant occurredAt;
    private final Instant enqueuedAt;
    private Instant processedAt;
    private OutboxStatus status;
    private int attempts;
    private final String partitionKey;
    private String lastError;

    public OutboxMessage(String id, String aggregateType, String aggregateId, String eventType,
                        int version, String payloadJson, Map<String, String> headers,
                        Instant occurredAt, Instant enqueuedAt, String partitionKey) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.aggregateType = Objects.requireNonNull(aggregateType, "Aggregate type cannot be null");
        this.aggregateId = Objects.requireNonNull(aggregateId, "Aggregate ID cannot be null");
        this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null");
        this.version = version;
        this.payloadJson = Objects.requireNonNull(payloadJson, "Payload JSON cannot be null");
        this.headers = Objects.requireNonNull(headers, "Headers cannot be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
        this.enqueuedAt = Objects.requireNonNull(enqueuedAt, "Enqueued at cannot be null");
        this.partitionKey = Objects.requireNonNull(partitionKey, "Partition key cannot be null");
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
    }

    public String getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getVersion() {
        return version;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getEnqueuedAt() {
        return enqueuedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public String getLastError() {
        return lastError;
    }

    /**
     * Marks the message as sent.
     */
    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.processedAt = Instant.now();
    }

    /**
     * Marks the message as failed with an error message.
     * 
     * @param error the error message
     */
    public void markFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.lastError = error;
        this.attempts++;
        this.processedAt = Instant.now();
    }

    /**
     * Increments the attempt counter.
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutboxMessage that = (OutboxMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OutboxMessage{id='" + id + "', aggregateType='" + aggregateType + 
               "', aggregateId='" + aggregateId + "', eventType='" + eventType + 
               "', version=" + version + ", status=" + status + ", attempts=" + attempts + "}";
    }
}
