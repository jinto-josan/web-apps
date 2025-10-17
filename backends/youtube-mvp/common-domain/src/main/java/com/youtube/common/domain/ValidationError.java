package com.youtube.common.domain;

import java.util.Objects;

/**
 * Represents a validation error for a specific field.
 * Contains field name, error message, and error code.
 */
public class ValidationError {
    private final String field;
    private final String message;
    private final String code;

    public ValidationError(String field, String message, String code) {
        this.field = Objects.requireNonNull(field, "Field cannot be null");
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.code = Objects.requireNonNull(code, "Code cannot be null");
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationError that = (ValidationError) o;
        return Objects.equals(field, that.field) &&
               Objects.equals(message, that.message) &&
               Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, message, code);
    }

    @Override
    public String toString() {
        return "ValidationError{field='" + field + "', message='" + message + "', code='" + code + "'}";
    }
}
