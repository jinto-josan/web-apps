package com.youtube.channelservice.application.commands;

import com.youtube.channelservice.domain.models.Role;

/**
 * Command for setting member role following the sequence diagram flow.
 * Implements the Command Pattern with proper authorization and validation.
 */
public class SetMemberRoleCommand implements Command<Void> {
    
    private final String commandId;
    private final String channelId;
    private final String actorUserId;
    private final String targetUserId;
    private final Role newRole;
    
    // Dependencies injected via constructor
    private final ChannelCommandHandler commandHandler;
    
    public SetMemberRoleCommand(String commandId, String channelId, String actorUserId, 
                               String targetUserId, Role newRole,
                               ChannelCommandHandler commandHandler) {
        this.commandId = commandId;
        this.channelId = channelId;
        this.actorUserId = actorUserId;
        this.targetUserId = targetUserId;
        this.newRole = newRole;
        this.commandHandler = commandHandler;
    }
    
    @Override
    public Void execute() throws CommandExecutionException {
        try {
            commandHandler.setMemberRole(this);
            return null;
        } catch (Exception e) {
            throw new CommandExecutionException(
                getCommandType(), 
                commandId, 
                "MEMBER_ROLE_SET_FAILED", 
                "Failed to set member role: " + e.getMessage(), 
                e
            );
        }
    }
    
    @Override
    public String getCommandType() {
        return "SET_MEMBER_ROLE";
    }
    
    @Override
    public String getCommandId() {
        return commandId;
    }
    
    // Getters for command data
    public String getChannelId() { return channelId; }
    public String getActorUserId() { return actorUserId; }
    public String getTargetUserId() { return targetUserId; }
    public Role getNewRole() { return newRole; }
}
