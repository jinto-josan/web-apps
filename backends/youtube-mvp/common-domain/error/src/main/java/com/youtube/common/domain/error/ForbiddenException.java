package com.youtube.common.domain.error;

/**
 * Exception thrown when an authenticated user is not authorized to perform an operation.
 */
public class ForbiddenException extends DomainException {
    
    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
    
    public ForbiddenException() {
        super("FORBIDDEN", "Access denied");
    }
}

