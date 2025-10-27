package com.youtube.channelservice.application.commands;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.models.Branding;

import java.time.Instant;

/**
 * Command for creating a new channel following the sequence diagram flow.
 * Implements the Command Pattern with proper error handling and validation.
 */
public class CreateChannelCommand implements Command<Channel> {
    
    private final String commandId;
    private final String ownerUserId;
    private final String handle;
    private final String title;
    private final String description;
    private final String language;
    private final String country;
    
    // Dependencies injected via constructor
    private final ChannelCommandHandler commandHandler;
    
    public CreateChannelCommand(String commandId, String ownerUserId, String handle, 
                               String title, String description, String language, String country,
                               ChannelCommandHandler commandHandler) {
        this.commandId = commandId;
        this.ownerUserId = ownerUserId;
        this.handle = handle;
        this.title = title;
        this.description = description;
        this.language = language;
        this.country = country;
        this.commandHandler = commandHandler;
    }
    
    @Override
    public Channel execute() throws CommandExecutionException {
        try {
            return commandHandler.createChannel(this);
        } catch (Exception e) {
            throw new CommandExecutionException(
                getCommandType(), 
                commandId, 
                "CHANNEL_CREATION_FAILED", 
                "Failed to create channel: " + e.getMessage(), 
                e
            );
        }
    }
    
    @Override
    public String getCommandType() {
        return "CREATE_CHANNEL";
    }
    
    @Override
    public String getCommandId() {
        return commandId;
    }
    
    // Getters for command data
    public String getOwnerUserId() { return ownerUserId; }
    public String getHandle() { return handle; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLanguage() { return language; }
    public String getCountry() { return country; }
}