package com.youtube.common.domain.events.outbox;

import com.youtube.common.domain.persistence.entity.OutboxEvent;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for outbox events.
 * Handles storage and retrieval of events for the transactional outbox pattern.
 */
public interface OutboxRepository {
    
    /**
     * Appends domain events to the outbox.
     * 
     * @param events the events to append
     * @param correlationId the correlation ID
     * @param causationId the causation ID
     * @param traceparent the traceparent header
     */
    void append(List<OutboxEventData> events, String correlationId, String causationId, String traceparent);
    
    /**
     * Finds pending outbox events (not yet dispatched).
     * Uses SELECT ... FOR UPDATE SKIP LOCKED for concurrent processing.
     * 
     * @param limit maximum number of events to fetch
     * @return list of pending events
     */
    List<OutboxEvent> fetchPendingBatch(int limit);
    
    /**
     * Marks an event as dispatched.
     * 
     * @param eventId the event ID
     * @param brokerMessageId the message ID from the broker
     */
    void markDispatched(String eventId, String brokerMessageId);
    
    /**
     * Marks an event as failed.
     * 
     * @param eventId the event ID
     * @param error the error message
     */
    void markFailed(String eventId, String error);
    
    /**
     * Finds an outbox event by ID.
     * 
     * @param eventId the event ID
     * @return the event, or empty if not found
     */
    Optional<OutboxEvent> findById(String eventId);
    
    /**
     * Data transfer object for outbox events.
     */
    record OutboxEventData(
        String eventType,
        String aggregateType,
        String aggregateId,
        String payloadJson
    ) {}
}

