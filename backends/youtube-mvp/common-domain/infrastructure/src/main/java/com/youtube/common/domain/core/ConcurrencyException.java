package com.youtube.common.domain.core;

import com.youtube.common.domain.error.ConflictException;

/**
 * Exception thrown when an optimistic concurrency conflict is detected.
 * This occurs when trying to save an aggregate with a version that doesn't match
 * the current version in the database.
 * 
 * <p>This extends ConflictException from the error module to integrate
 * with common error handling.</p>
 */
public class ConcurrencyException extends ConflictException {
    
    public ConcurrencyException(String message) {
        super(message);
    }
    
    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}

