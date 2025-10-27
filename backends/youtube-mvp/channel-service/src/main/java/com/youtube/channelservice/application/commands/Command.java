package com.youtube.channelservice.application.commands;

/**
 * Base interface for all commands in the Command Pattern implementation.
 * Commands encapsulate a request as an object, allowing for parameterization,
 * queuing, logging, and undo operations.
 */
public interface Command<T> {
    
    /**
     * Executes the command and returns the result.
     * @return The result of command execution
     * @throws CommandExecutionException if the command fails to execute
     */
    T execute() throws CommandExecutionException;
    
    /**
     * Gets the command type for logging and debugging purposes.
     * @return The command type identifier
     */
    String getCommandType();
    
    /**
     * Gets the command ID for tracking and correlation.
     * @return Unique command identifier
     */
    String getCommandId();
}
