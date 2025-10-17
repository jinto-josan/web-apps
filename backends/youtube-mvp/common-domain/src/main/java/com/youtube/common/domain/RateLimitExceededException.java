package com.youtube.common.domain;

/**
 * Exception thrown when rate limiting is exceeded.
 */
public class RateLimitExceededException extends DomainException {
    
    public RateLimitExceededException(String message) {
        super("RATE_LIMIT_EXCEEDED", message, true);
    }
    
    public RateLimitExceededException(String operation, int limit) {
        super("RATE_LIMIT_EXCEEDED", "Rate limit exceeded for " + operation + " (limit: " + limit + ")", true);
    }
}
