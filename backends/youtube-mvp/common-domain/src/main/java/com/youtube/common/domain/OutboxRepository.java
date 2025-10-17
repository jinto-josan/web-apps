package com.youtube.common.domain;

import java.util.List;

/**
 * Repository interface for managing outbox messages.
 * Handles persistence operations for the outbox pattern.
 */
public interface OutboxRepository {
    
    /**
     * Saves an outbox message.
     * 
     * @param message the outbox message to save
     */
    void save(OutboxMessage message);
    
    /**
     * Fetches a batch of messages with the specified status.
     * 
     * @param status the status to filter by
     * @param limit the maximum number of messages to return
     * @return list of outbox messages
     */
    List<OutboxMessage> fetchBatch(OutboxStatus status, int limit);
    
    /**
     * Marks a message as sent.
     * 
     * @param id the ID of the message to mark as sent
     */
    void markSent(String id);
    
    /**
     * Marks a message as failed with an error message.
     * 
     * @param id the ID of the message to mark as failed
     * @param error the error message
     */
    void markFailed(String id, String error);
    
    /**
     * Deletes a message from the outbox.
     * 
     * @param id the ID of the message to delete
     */
    void delete(String id);
}
