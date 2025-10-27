package com.youtube.channelservice.shared.exceptions;

/**
 * Exception thrown when a resource is not found.
 * Represents 404 Not Found scenarios.
 */
public class NotFoundException extends RuntimeException {
    
    private final String errorCode;
    
    public NotFoundException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
    
    public NotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public NotFoundException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
