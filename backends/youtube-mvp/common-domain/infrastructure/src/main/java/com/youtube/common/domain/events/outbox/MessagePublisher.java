package com.youtube.common.domain.events.outbox;

import com.youtube.common.domain.persistence.entity.OutboxEvent;

/**
 * Abstraction for publishing domain events to message brokers.
 * 
 * <p>This interface allows the OutboxDispatcher to work with different
 * messaging systems (Azure Service Bus, Kafka, RabbitMQ, etc.) without
 * being tightly coupled to a specific implementation.</p>
 */
public interface MessagePublisher {
    
    /**
     * Publishes a domain event to the message broker.
     * 
     * @param event the outbox event to publish
     * @throws MessagePublishException if publishing fails
     */
    void publish(OutboxEvent event);
    
    /**
     * Gets the message ID returned by the broker after publishing.
     * This can be used for tracking and idempotency.
     * 
     * @param event the event that was published
     * @return the broker message ID, or null if not available
     */
    String getBrokerMessageId(OutboxEvent event);
    
    /**
     * Exception thrown when message publishing fails.
     */
    class MessagePublishException extends RuntimeException {
        public MessagePublishException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public MessagePublishException(String message) {
            super(message);
        }
    }
}

