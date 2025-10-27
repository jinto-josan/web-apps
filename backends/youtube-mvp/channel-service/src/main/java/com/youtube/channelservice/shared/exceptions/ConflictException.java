package com.youtube.channelservice.shared.exceptions;

/**
 * Exception thrown when there's a conflict with the current state.
 * Represents 409 Conflict scenarios.
 */
public class ConflictException extends RuntimeException {
    
    private final String errorCode;
    
    public ConflictException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
    
    public ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ConflictException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
