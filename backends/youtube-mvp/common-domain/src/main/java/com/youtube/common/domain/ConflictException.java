package com.youtube.common.domain;

/**
 * Exception thrown when a conflict occurs (e.g., duplicate resource).
 */
public class ConflictException extends DomainException {
    
    public ConflictException(String message) {
        super("CONFLICT", message, false);
    }
    
    public ConflictException(String resource, String reason) {
        super("CONFLICT", resource + " conflict: " + reason, false);
    }
}
