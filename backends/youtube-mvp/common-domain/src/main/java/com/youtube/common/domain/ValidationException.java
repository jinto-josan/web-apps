package com.youtube.common.domain;

import java.util.List;
import java.util.Objects;

/**
 * Exception thrown when validation fails.
 * Contains a list of validation errors.
 */
public class ValidationException extends DomainException {
    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super("VALIDATION_FAILED", "Validation failed", false);
        this.errors = Objects.requireNonNull(errors, "Errors cannot be null");
    }

    public ValidationException(String message, List<ValidationError> errors) {
        super("VALIDATION_FAILED", message, false);
        this.errors = Objects.requireNonNull(errors, "Errors cannot be null");
    }

    /**
     * Gets the list of validation errors.
     * 
     * @return list of validation errors
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "ValidationException{message='" + getMessage() + "', errors=" + errors + "}";
    }
}
