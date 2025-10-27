package com.youtube.channelservice.shared.exceptions;

/**
 * Exception thrown when input validation fails.
 * Represents 400 Bad Request scenarios.
 */
public class ValidationException extends RuntimeException {
    
    private final String errorCode;
    
    public ValidationException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
    
    public ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ValidationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
