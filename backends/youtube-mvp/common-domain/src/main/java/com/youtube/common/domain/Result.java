package com.youtube.common.domain;

/**
 * Interface representing the result of an operation that can either succeed or fail.
 * Implements the Result pattern for functional error handling.
 */
public interface Result<T> {
    
    /**
     * Checks if the result is successful.
     * 
     * @return true if the result is successful
     */
    boolean isSuccess();
    
    /**
     * Checks if the result is an error.
     * 
     * @return true if the result is an error
     */
    boolean isError();
    
    /**
     * Gets the value if the result is successful.
     * 
     * @return the value
     * @throws IllegalStateException if the result is an error
     */
    T getValue();
    
    /**
     * Gets the error if the result is an error.
     * 
     * @return the error
     * @throws IllegalStateException if the result is successful
     */
    DomainException getError();
    
    /**
     * Creates a successful result with a value.
     * 
     * @param value the value
     * @param <T> the type of the value
     * @return a successful result
     */
    static <T> Result<T> ok(T value) {
        return new Ok<>(value);
    }
    
    /**
     * Creates an error result with an exception.
     * 
     * @param error the error
     * @param <T> the type of the value
     * @return an error result
     */
    static <T> Result<T> err(DomainException error) {
        return new Err<>(error);
    }
}
