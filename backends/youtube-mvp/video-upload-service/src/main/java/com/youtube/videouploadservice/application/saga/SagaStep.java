package com.youtube.videouploadservice.application.saga;

/**
 * Represents a single step in a saga.
 * Each step can be executed and compensated if needed.
 */
public interface SagaStep {
    
    /**
     * Executes this saga step.
     * @param context The saga execution context
     * @return The result of step execution
     * @throws SagaStepException if the step fails to execute
     */
    Object execute(SagaContext context) throws SagaStepException;
    
    /**
     * Compensates this saga step if it needs to be rolled back.
     * @param context The saga execution context
     * @throws SagaStepException if compensation fails
     */
    void compensate(SagaContext context) throws SagaStepException;
    
    /**
     * Gets the step name for logging and debugging.
     * @return The step name
     */
    String getStepName();
    
    /**
     * Checks if this step can be compensated.
     * @return true if the step can be compensated, false otherwise
     */
    boolean isCompensatable();
}

