package com.youtube.common.domain.error;

/**
 * Exception thrown when an operation requires authentication but none is provided.
 */
public class UnauthorizedException extends DomainException {
    
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
    
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Authentication required");
    }
}

