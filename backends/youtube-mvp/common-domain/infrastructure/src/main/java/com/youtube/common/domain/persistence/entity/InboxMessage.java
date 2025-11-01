package com.youtube.common.domain.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class for inbox pattern entities (idempotent event processing).
 * 
 * <p>This abstract entity implements the inbox pattern, ensuring idempotent
 * processing of incoming events from message brokers. The pattern prevents
 * duplicate event processing by tracking which messages have already been
 * processed.</p>
 * 
 * <p>Services should extend this class and configure it with their schema-specific
 * table mapping:</p>
 * 
 * <pre>{@code
 * @Entity
 * @Table(name = "inbox_messages", schema = "your_schema")
 * public class YourInboxMessage extends InboxMessage {
 * }
 * }</pre>
 * 
 * <p>The inbox pattern ensures:</p>
 * <ul>
 *   <li>Events are processed exactly once</li>
 *   <li>Duplicate messages are detected and ignored</li>
 *   <li>Processing failures can be retried</li>
 * </ul>
 * 
 * @see <a href="https://microservices.io/patterns/data/inbox.html">Inbox Pattern</a>
 */
@MappedSuperclass
@Getter
@Setter
public abstract class InboxMessage {

    /**
     * Unique message identifier from the message broker.
     * Typically the message ID from Service Bus, Kafka offset, etc.
     */
    @Id
    @Column(name = "message_id", length = 128, nullable = false, updatable = false)
    private String messageId;

    /**
     * Timestamp when the message was first seen by the service.
     */
    @Column(name = "first_seen_at", nullable = false, updatable = false)
    private Instant firstSeenAt;

    /**
     * Timestamp when the message was successfully processed.
     * Null if not yet processed or if processing failed.
     */
    @Column(name = "processed_at")
    private Instant processedAt;

    /**
     * Number of processing attempts (including retries).
     */
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    /**
     * Timestamp of the last processing attempt.
     */
    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    /**
     * Error message from the last failed processing attempt.
     */
    @Column(name = "error", length = 2000)
    private String error;

    /**
     * Sets the first seen timestamp before persisting if not already set.
     */
    @PrePersist
    void prePersist() {
        if (firstSeenAt == null) {
            firstSeenAt = Instant.now();
        }
    }

    /**
     * Checks if this message has been successfully processed.
     * 
     * @return true if processedAt is not null, false otherwise
     */
    public boolean isProcessed() {
        return processedAt != null;
    }

    /**
     * Checks if this message has failed processing.
     * 
     * @return true if error is not null and not empty, false otherwise
     */
    public boolean hasError() {
        return error != null && !error.isBlank();
    }

    /**
     * Records a processing attempt by incrementing the attempt counter
     * and updating the last attempt timestamp.
     */
    public void recordAttempt() {
        attempts++;
        lastAttemptAt = Instant.now();
    }

    /**
     * Marks the message as successfully processed.
     */
    public void markProcessed() {
        processedAt = Instant.now();
        error = null;
    }

    /**
     * Records a processing failure with the given error message.
     * 
     * @param errorMessage the error message describing the failure
     */
    public void recordFailure(String errorMessage) {
        recordAttempt();
        error = errorMessage != null && errorMessage.length() > 2000 
                ? errorMessage.substring(0, 2000) 
                : errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InboxMessage that = (InboxMessage) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "InboxMessage{" +
                "messageId='" + messageId + '\'' +
                ", firstSeenAt=" + firstSeenAt +
                ", processedAt=" + processedAt +
                ", attempts=" + attempts +
                ", hasError=" + hasError() +
                '}';
    }
}

