package com.youtube.common.domain.error;

import java.util.function.Function;

/**
 * Result type for operations that can fail.
 * Provides a functional approach to error handling without exceptions.
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * Result<User> result = userService.findUser(userId);
 * 
 * result.ifSuccess(user -> {
 *     // Handle success
 * }).ifFailure(error -> {
 *     // Handle error
 * });
 * 
 * // Or use map/flatMap for chaining
 * Result<String> email = result.map(User::getEmail);
 * }</pre>
 * 
 * @param <T> the success value type
 */
public sealed interface Result<T> {
    
    /**
     * Creates a successful result.
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }
    
    /**
     * Creates a failed result.
     */
    static <T> Result<T> failure(String errorCode, String message) {
        return new Failure<>(errorCode, message);
    }
    
    /**
     * Creates a failed result from a domain exception.
     */
    static <T> Result<T> failure(DomainException exception) {
        return new Failure<>(exception.getErrorCode(), exception.getMessage());
    }
    
    /**
     * Checks if the result is successful.
     */
    boolean isSuccess();
    
    /**
     * Checks if the result is a failure.
     */
    default boolean isFailure() {
        return !isSuccess();
    }
    
    /**
     * Gets the value if successful, otherwise throws an exception.
     */
    T getValue();
    
    /**
     * Gets the error code if failed.
     */
    String getErrorCode();
    
    /**
     * Gets the error message if failed.
     */
    String getErrorMessage();
    
    /**
     * Executes the function if the result is successful.
     */
    Result<T> ifSuccess(java.util.function.Consumer<T> consumer);
    
    /**
     * Executes the function if the result is a failure.
     */
    Result<T> ifFailure(java.util.function.Consumer<String> errorHandler);
    
    /**
     * Maps the success value to another type.
     */
    <U> Result<U> map(Function<T, U> mapper);
    
    /**
     * Flat maps the result to another result.
     */
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);
    
    /**
     * Success case.
     */
    record Success<T>(T value) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }
        
        @Override
        public T getValue() {
            return value;
        }
        
        @Override
        public String getErrorCode() {
            throw new IllegalStateException("Cannot get error code from success result");
        }
        
        @Override
        public String getErrorMessage() {
            throw new IllegalStateException("Cannot get error message from success result");
        }
        
        @Override
        public Result<T> ifSuccess(java.util.function.Consumer<T> consumer) {
            consumer.accept(value);
            return this;
        }
        
        @Override
        public Result<T> ifFailure(java.util.function.Consumer<String> errorHandler) {
            return this;
        }
        
        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            try {
                return Result.success(mapper.apply(value));
            } catch (Exception e) {
                return Result.failure("MAPPING_FAILED", e.getMessage());
            }
        }
        
        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            try {
                return mapper.apply(value);
            } catch (Exception e) {
                return Result.failure("MAPPING_FAILED", e.getMessage());
            }
        }
    }
    
    /**
     * Failure case.
     */
    record Failure<T>(String errorCode, String errorMessage) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }
        
        @Override
        public T getValue() {
            throw new IllegalStateException("Cannot get value from failure result: " + errorMessage);
        }
        
        @Override
        public String getErrorCode() {
            return errorCode;
        }
        
        @Override
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public Result<T> ifSuccess(java.util.function.Consumer<T> consumer) {
            return this;
        }
        
        @Override
        public Result<T> ifFailure(java.util.function.Consumer<String> errorHandler) {
            errorHandler.accept(errorMessage);
            return this;
        }
        
        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            return new Failure<>(errorCode, errorMessage);
        }
        
        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            return new Failure<>(errorCode, errorMessage);
        }
    }
}

