package com.youtube.channelservice.application.saga;

/**
 * Exception thrown when a saga step fails to execute.
 * Contains details about the step failure for proper error handling.
 */
public class SagaStepException extends RuntimeException {
    
    private final String stepName;
    private final String sagaId;
    
    public SagaStepException(String stepName, String sagaId, String message) {
        super(message);
        this.stepName = stepName;
        this.sagaId = sagaId;
    }
    
    public SagaStepException(String stepName, String sagaId, String message, Throwable cause) {
        super(message, cause);
        this.stepName = stepName;
        this.sagaId = sagaId;
    }
    
    public String getStepName() { return stepName; }
    public String getSagaId() { return sagaId; }
}
