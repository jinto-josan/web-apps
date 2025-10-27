package com.youtube.channelservice.application.commands;

import com.youtube.channelservice.domain.models.Channel;

import java.time.Instant;

/**
 * Command for changing a channel's handle following the sequence diagram flow.
 * Implements the Command Pattern with proper validation and rollback capability.
 */
public class ChangeHandleCommand implements Command<Channel> {
    
    private final String commandId;
    private final String channelId;
    private final String actorUserId;
    private final String newHandle;
    private final String ifMatchEtag;
    private final Instant lastChangeAt;
    
    // Dependencies injected via constructor
    private final ChannelCommandHandler commandHandler;
    
    public ChangeHandleCommand(String commandId, String channelId, String actorUserId, 
                              String newHandle, String ifMatchEtag, Instant lastChangeAt,
                              ChannelCommandHandler commandHandler) {
        this.commandId = commandId;
        this.channelId = channelId;
        this.actorUserId = actorUserId;
        this.newHandle = newHandle;
        this.ifMatchEtag = ifMatchEtag;
        this.lastChangeAt = lastChangeAt;
        this.commandHandler = commandHandler;
    }
    
    @Override
    public Channel execute() throws CommandExecutionException {
        try {
            return commandHandler.changeHandle(this);
        } catch (Exception e) {
            throw new CommandExecutionException(
                getCommandType(), 
                commandId, 
                "HANDLE_CHANGE_FAILED", 
                "Failed to change handle: " + e.getMessage(), 
                e
            );
        }
    }
    
    @Override
    public String getCommandType() {
        return "CHANGE_HANDLE";
    }
    
    @Override
    public String getCommandId() {
        return commandId;
    }
    
    // Getters for command data
    public String getChannelId() { return channelId; }
    public String getActorUserId() { return actorUserId; }
    public String getNewHandle() { return newHandle; }
    public String getIfMatchEtag() { return ifMatchEtag; }
    public Instant getLastChangeAt() { return lastChangeAt; }
}
