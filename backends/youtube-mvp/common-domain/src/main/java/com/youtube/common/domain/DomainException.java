package com.youtube.common.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class for domain exceptions.
 * Provides common properties for all domain-specific exceptions.
 */
public abstract class DomainException extends RuntimeException {
    private final String code;
    private final boolean retryable;
    private final Map<String, Object> details;

    protected DomainException(String code, String message, boolean retryable, Map<String, Object> details) {
        super(message);
        this.code = Objects.requireNonNull(code, "Error code cannot be null");
        this.retryable = retryable;
        this.details = details != null ? Map.copyOf(details) : Map.of();
    }

    protected DomainException(String code, String message, boolean retryable) {
        this(code, message, retryable, null);
    }

    protected DomainException(String code, String message) {
        this(code, message, false);
    }

    /**
     * Gets the error code for this exception.
     * 
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Indicates whether this exception is retryable.
     * 
     * @return true if the operation can be retried
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Gets additional details about this exception.
     * 
     * @return map of additional details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{code='" + code + "', message='" + getMessage() + 
               "', retryable=" + retryable + "}";
    }
}
