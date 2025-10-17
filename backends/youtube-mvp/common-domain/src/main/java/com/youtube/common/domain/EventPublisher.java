package com.youtube.common.domain;

import java.util.List;

/**
 * Interface for publishing domain events.
 * Implementations handle the actual publishing mechanism (message queues, etc.).
 */
public interface EventPublisher {
    
    /**
     * Publishes a single domain event.
     * 
     * @param event the domain event to publish
     */
    void publish(DomainEvent event);
    
    /**
     * Publishes multiple domain events.
     * 
     * @param events the list of domain events to publish
     */
    void publishAll(List<DomainEvent> events);
    
    /**
     * Publishes an event envelope with metadata.
     * 
     * @param envelope the event envelope to publish
     */
    void publishEnvelope(EventEnvelope envelope);
}
