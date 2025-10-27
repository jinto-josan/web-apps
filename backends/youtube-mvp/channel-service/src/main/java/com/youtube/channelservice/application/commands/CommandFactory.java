package com.youtube.channelservice.application.commands;

import com.youtube.channelservice.domain.models.Branding;
import com.youtube.channelservice.domain.models.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Factory for creating command objects.
 * Handles command instantiation with proper dependency injection.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommandFactory {
    
    private final ChannelCommandHandler commandHandler;
    
    /**
     * Creates a CreateChannelCommand.
     */
    public CreateChannelCommand createChannelCommand(String ownerUserId, String handle, 
                                                    String title, String description, 
                                                    String language, String country) {
        String commandId = UUID.randomUUID().toString();
        return new CreateChannelCommand(commandId, ownerUserId, handle, title, description, 
                language, country, commandHandler);
    }
    
    /**
     * Creates a ChangeHandleCommand.
     */
    public ChangeHandleCommand changeHandleCommand(String channelId, String actorUserId, 
                                                  String newHandle, String ifMatchEtag, 
                                                  Instant lastChangeAt) {
        String commandId = UUID.randomUUID().toString();
        return new ChangeHandleCommand(commandId, channelId, actorUserId, newHandle, 
                ifMatchEtag, lastChangeAt, commandHandler);
    }
    
    /**
     * Creates an UpdateBrandingCommand.
     */
    public UpdateBrandingCommand updateBrandingCommand(String channelId, String actorUserId, 
                                                       Branding branding, String ifMatchEtag) {
        String commandId = UUID.randomUUID().toString();
        return new UpdateBrandingCommand(commandId, channelId, actorUserId, branding, 
                ifMatchEtag, commandHandler);
    }
    
    /**
     * Creates a SetMemberRoleCommand.
     */
    public SetMemberRoleCommand setMemberRoleCommand(String channelId, String actorUserId, 
                                                    String targetUserId, Role newRole) {
        String commandId = UUID.randomUUID().toString();
        return new SetMemberRoleCommand(commandId, channelId, actorUserId, targetUserId, 
                newRole, commandHandler);
    }
}
