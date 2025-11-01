package com.youtube.common.domain.error;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 * Contains validation error details.
 */
public class ValidationException extends DomainException {
    
    private final Map<String, List<String>> validationErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_FAILED", message);
        this.validationErrors = Map.of();
    }
    
    public ValidationException(String message, Map<String, List<String>> validationErrors) {
        super("VALIDATION_FAILED", message);
        this.validationErrors = validationErrors != null ? validationErrors : Map.of();
    }
    
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_FAILED", message, cause);
        this.validationErrors = Map.of();
    }
    
    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }
    
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
}

