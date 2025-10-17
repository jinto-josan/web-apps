package com.youtube.common.domain;

/**
 * Exception thrown when a precondition fails.
 */
public class PreconditionFailedException extends DomainException {
    
    public PreconditionFailedException(String message) {
        super("PRECONDITION_FAILED", message, false);
    }
    
    public PreconditionFailedException(String precondition, String reason) {
        super("PRECONDITION_FAILED", "Precondition '" + precondition + "' failed: " + reason, false);
    }
}
