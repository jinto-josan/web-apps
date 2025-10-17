package com.youtube.common.domain;

/**
 * Interface for validating objects.
 * Provides a contract for validation logic.
 */
public interface Validator<T> {
    
    /**
     * Validates the target object.
     * 
     * @param target the object to validate
     * @throws ValidationException if validation fails
     */
    void validate(T target);
}
