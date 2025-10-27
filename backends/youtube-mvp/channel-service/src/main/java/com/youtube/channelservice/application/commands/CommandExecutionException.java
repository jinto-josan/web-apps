package com.youtube.channelservice.application.commands;

/**
 * Exception thrown when a command fails to execute.
 * Contains details about the failure for proper error handling.
 */
public class CommandExecutionException extends RuntimeException {
    
    private final String commandType;
    private final String commandId;
    private final String errorCode;
    
    public CommandExecutionException(String commandType, String commandId, String errorCode, String message) {
        super(message);
        this.commandType = commandType;
        this.commandId = commandId;
        this.errorCode = errorCode;
    }
    
    public CommandExecutionException(String commandType, String commandId, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.commandType = commandType;
        this.commandId = commandId;
        this.errorCode = errorCode;
    }
    
    public String getCommandType() { return commandType; }
    public String getCommandId() { return commandId; }
    public String getErrorCode() { return errorCode; }
}
