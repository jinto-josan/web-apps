package com.youtube.channelservice.shared.exceptions;

/**
 * Exception thrown when access is forbidden.
 * Represents 403 Forbidden scenarios.
 */
public class ForbiddenException extends RuntimeException {
    
    private final String errorCode;
    
    public ForbiddenException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
    
    public ForbiddenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ForbiddenException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
