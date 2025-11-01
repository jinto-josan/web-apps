package com.youtube.common.domain.error;

/**
 * Common error codes used across services.
 * Provides consistent error code constants.
 */
public final class ErrorCodes {
    
    private ErrorCodes() {
        // Utility class
    }
    
    // Domain Errors (4xx)
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    
    // Concurrency
    public static final String CONCURRENCY_CONFLICT = "CONCURRENCY_CONFLICT";
    public static final String VERSION_CONFLICT = "VERSION_CONFLICT";
    
    // Idempotency
    public static final String IDEMPOTENCY_KEY_MISMATCH = "IDEMPOTENCY_KEY_MISMATCH";
    
    // System Errors (5xx)
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    public static final String TIMEOUT = "TIMEOUT";
    
    // Event Processing
    public static final String EVENT_PROCESSING_FAILED = "EVENT_PROCESSING_FAILED";
    public static final String EVENT_DESERIALIZATION_FAILED = "EVENT_DESERIALIZATION_FAILED";
    
    // External Service Errors
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String CACHE_ERROR = "CACHE_ERROR";
}

