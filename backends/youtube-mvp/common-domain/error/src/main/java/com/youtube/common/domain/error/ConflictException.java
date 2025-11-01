package com.youtube.common.domain.error;

/**
 * Exception thrown when an operation conflicts with the current state.
 * Typically used for optimistic concurrency conflicts or business rule violations.
 */
public class ConflictException extends DomainException {
    
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super("CONFLICT", message, cause);
    }
}

