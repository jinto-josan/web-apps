package com.youtube.common.domain;

/**
 * Enum representing the status of an outbox message.
 */
public enum OutboxStatus {
    /**
     * Message is pending processing.
     */
    PENDING,
    
    /**
     * Message has been successfully sent.
     */
    SENT,
    
    /**
     * Message processing failed.
     */
    FAILED
}
