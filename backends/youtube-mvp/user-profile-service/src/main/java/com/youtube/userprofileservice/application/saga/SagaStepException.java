package com.youtube.userprofileservice.application.saga;

/**
 * Exception thrown when a saga step execution fails.
 */
public class SagaStepException extends Exception {
    
    private final String stepName;
    private final String sagaId;
    private final String errorCode;
    
    public SagaStepException(String stepName, String sagaId, String errorCode) {
        super("Saga step failed: " + stepName + " (Saga: " + sagaId + ", Error: " + errorCode + ")");
        this.stepName = stepName;
        this.sagaId = sagaId;
        this.errorCode = errorCode;
    }
    
    public SagaStepException(String stepName, String sagaId, String errorCode, Throwable cause) {
        super("Saga step failed: " + stepName + " (Saga: " + sagaId + ", Error: " + errorCode + ")", cause);
        this.stepName = stepName;
        this.sagaId = sagaId;
        this.errorCode = errorCode;
    }
    
    public String getStepName() {
        return stepName;
    }
    
    public String getSagaId() {
        return sagaId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

