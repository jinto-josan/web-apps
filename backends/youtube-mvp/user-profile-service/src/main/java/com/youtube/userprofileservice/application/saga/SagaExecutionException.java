package com.youtube.userprofileservice.application.saga;

/**
 * Exception thrown when saga execution fails.
 */
public class SagaExecutionException extends Exception {
    
    private final String sagaId;
    private final String sagaType;
    private final String failedStep;
    
    public SagaExecutionException(String sagaId, String sagaType, String failedStep, String message, Throwable cause) {
        super(message, cause);
        this.sagaId = sagaId;
        this.sagaType = sagaType;
        this.failedStep = failedStep;
    }
    
    public String getSagaId() {
        return sagaId;
    }
    
    public String getSagaType() {
        return sagaType;
    }
    
    public String getFailedStep() {
        return failedStep;
    }
}

