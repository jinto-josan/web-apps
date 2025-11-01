package com.youtube.common.domain.error;

/**
 * Base exception for domain-related errors.
 * Represents business rule violations and domain constraints.
 * 
 * <p>Domain exceptions indicate that a business rule was violated.
 * These are expected errors that should be handled gracefully.</p>
 */
public class DomainException extends RuntimeException {
    
    private final String errorCode;
    
    public DomainException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
    
    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

