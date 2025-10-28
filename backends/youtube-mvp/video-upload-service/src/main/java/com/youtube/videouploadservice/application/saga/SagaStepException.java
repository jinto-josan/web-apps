package com.youtube.videouploadservice.application.saga;

/**
 * Exception thrown when a saga step fails to execute.
 * Used to trigger compensation in saga orchestration.
 */
public class SagaStepException extends RuntimeException {
    
    private final String stepName;
    private final String sagaId;
    private final String errorCode;
    
    public SagaStepException(String stepName, String sagaId, String errorCode) {
        super("Saga step failed: " + stepName + " (Saga: " + sagaId + ", Error: " + errorCode + ")");
        this.stepName = stepName;
        this.sagaId = sagaId;
        this.errorCode = errorCode;
    }
    
    public SagaStepException(String stepName, String sagaId, String errorCode, String message) {
        super("Saga step failed: " + stepName + " (Saga: " + sagaId + ", Error: " + errorCode + ") - " + message);
        this.stepName = stepName;
        this.sagaId = sagaId;
        this.errorCode = errorCode;
    }
    
    public SagaStepException(String stepName, String sagaId, String errorCode, String message, Throwable cause) {
        super("Saga step failed: " + stepName + " (Saga: " + sagaId + ", Error: " + errorCode + ") - " + message, cause);
        this.stepName = stepName;
        this.sagaId = sagaId;
        this.errorCode = errorCode;
    }
    
    public String getStepName() { return stepName; }
    public String getSagaId() { return sagaId; }
    public String getErrorCode() { return errorCode; }
}

