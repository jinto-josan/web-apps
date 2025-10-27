package com.youtube.channelservice.application.commands;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Branding;

/**
 * Command for updating channel branding following the sequence diagram flow.
 * Implements the Command Pattern with proper validation and ETag handling.
 */
public class UpdateBrandingCommand implements Command<Channel> {
    
    private final String commandId;
    private final String channelId;
    private final String actorUserId;
    private final Branding branding;
    private final String ifMatchEtag;
    
    // Dependencies injected via constructor
    private final ChannelCommandHandler commandHandler;
    
    public UpdateBrandingCommand(String commandId, String channelId, String actorUserId, 
                                Branding branding, String ifMatchEtag,
                                ChannelCommandHandler commandHandler) {
        this.commandId = commandId;
        this.channelId = channelId;
        this.actorUserId = actorUserId;
        this.branding = branding;
        this.ifMatchEtag = ifMatchEtag;
        this.commandHandler = commandHandler;
    }
    
    @Override
    public Channel execute() throws CommandExecutionException {
        try {
            return commandHandler.updateBranding(this);
        } catch (Exception e) {
            throw new CommandExecutionException(
                getCommandType(), 
                commandId, 
                "BRANDING_UPDATE_FAILED", 
                "Failed to update branding: " + e.getMessage(), 
                e
            );
        }
    }
    
    @Override
    public String getCommandType() {
        return "UPDATE_BRANDING";
    }
    
    @Override
    public String getCommandId() {
        return commandId;
    }
    
    // Getters for command data
    public String getChannelId() { return channelId; }
    public String getActorUserId() { return actorUserId; }
    public Branding getBranding() { return branding; }
    public String getIfMatchEtag() { return ifMatchEtag; }
}
