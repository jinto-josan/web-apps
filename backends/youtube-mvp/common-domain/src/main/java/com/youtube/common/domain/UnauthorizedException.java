package com.youtube.common.domain;

/**
 * Exception thrown when authorization fails.
 */
public class UnauthorizedException extends DomainException {
    
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, false);
    }
    
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Access denied", false);
    }
}
