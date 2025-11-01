package com.youtube.common.domain.events.inbox;

import com.youtube.common.domain.persistence.entity.InboxMessage;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository interface for inbox messages.
 * Handles idempotent event processing using the inbox pattern.
 */
public interface InboxRepository {
    
    /**
     * Attempts to begin processing a message.
     * Returns true if the message hasn't been processed before (idempotent check).
     * 
     * @param messageId the unique message ID from the broker
     * @return true if processing should proceed, false if already processed
     */
    boolean beginProcess(String messageId);
    
    /**
     * Marks a message as successfully processed.
     * 
     * @param messageId the message ID
     */
    void markProcessed(String messageId);
    
    /**
     * Records a processing failure.
     * 
     * @param messageId the message ID
     * @param error the error message
     */
    void recordFailure(String messageId, String error);
    
    /**
     * Finds an inbox message by ID.
     * 
     * @param messageId the message ID
     * @return the inbox message, or empty if not found
     */
    Optional<InboxMessage> findById(String messageId);
    
    /**
     * Checks if a message has been processed.
     * 
     * @param messageId the message ID
     * @return true if processed, false otherwise
     */
    boolean isProcessed(String messageId);
}

