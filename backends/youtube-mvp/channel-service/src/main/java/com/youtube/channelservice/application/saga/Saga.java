package com.youtube.channelservice.application.saga;

import java.util.List;

/**
 * Base interface for Saga Pattern implementation.
 * A Saga is a sequence of local transactions that can be compensated if needed.
 * Each saga step can be executed and potentially compensated.
 */
public interface Saga<T> {
    
    /**
     * Executes the saga and returns the result.
     * @return The result of saga execution
     * @throws SagaExecutionException if the saga fails to execute
     */
    T execute() throws SagaExecutionException;
    
    /**
     * Gets the saga ID for tracking and correlation.
     * @return Unique saga identifier
     */
    String getSagaId();
    
    /**
     * Gets the saga type for logging and debugging purposes.
     * @return The saga type identifier
     */
    String getSagaType();
    
    /**
     * Gets the list of steps in this saga.
     * @return List of saga steps
     */
    List<SagaStep> getSteps();
}
