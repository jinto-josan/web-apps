package com.youtube.userprofileservice.application.saga;

/**
 * Saga step interface for individual operations in a saga.
 */
public interface SagaStep {
    
    /**
     * Executes the saga step.
     * 
     * @param context the saga context
     * @return the result of the step execution
     * @throws SagaStepException if execution fails
     */
    Object execute(SagaContext context) throws SagaStepException;
    
    /**
     * Compensates for the saga step.
     * 
     * @param context the saga context
     * @throws SagaStepException if compensation fails
     */
    void compensate(SagaContext context) throws SagaStepException;
    
    /**
     * Gets the step name.
     * 
     * @return the step name
     */
    String getStepName();
    
    /**
     * Returns whether this step is compensatable.
     * 
     * @return true if the step can be compensated
     */
    boolean isCompensatable();
}

