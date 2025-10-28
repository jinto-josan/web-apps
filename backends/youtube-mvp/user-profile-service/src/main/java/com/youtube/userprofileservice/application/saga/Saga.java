package com.youtube.userprofileservice.application.saga;

import java.util.List;

/**
 * Saga interface for orchestrating multi-step operations with compensation.
 */
public interface Saga<T> {
    
    /**
     * Executes the saga.
     * 
     * @return the result of the saga execution
     * @throws SagaExecutionException if execution fails
     */
    T execute() throws SagaExecutionException;
    
    /**
     * Gets the saga ID.
     * 
     * @return the saga ID
     */
    String getSagaId();
    
    /**
     * Gets the saga type.
     * 
     * @return the saga type
     */
    String getSagaType();
    
    /**
     * Gets the list of steps in the saga.
     * 
     * @return the list of saga steps
     */
    List<SagaStep> getSteps();
}

