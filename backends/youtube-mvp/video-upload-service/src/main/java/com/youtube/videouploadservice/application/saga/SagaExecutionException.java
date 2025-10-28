package com.youtube.videouploadservice.application.saga;

/**
 * Exception thrown when a saga fails to execute.
 * Contains details about the failure for proper error handling.
 */
public class SagaExecutionException extends RuntimeException {
    
    private final String sagaId;
    private final String sagaType;
    private final String failedStep;
    
    public SagaExecutionException(String sagaId, String sagaType, String failedStep, String message) {
        super(message);
        this.sagaId = sagaId;
        this.sagaType = sagaType;
        this.failedStep = failedStep;
    }
    
    public SagaExecutionException(String sagaId, String sagaType, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.sagaId = sagaId;
        this.sagaType = sagaType;
        this.failedStep = failedStep;
    }
    
    public String getSagaId() { return sagaId; }
    public String getSagaType() { return sagaType; }
    public String getFailedStep() { return failedStep; }
}

