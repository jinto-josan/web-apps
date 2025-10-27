package com.youtube.channelservice.application.commands;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.models.Branding;

/**
 * Handler interface for channel commands.
 * Implements the Command Pattern by providing a centralized way to execute commands.
 */
public interface ChannelCommandHandler {
    
    /**
     * Handles the create channel command following the sequence diagram flow.
     * @param command The create channel command
     * @return The created channel
     */
    Channel createChannel(CreateChannelCommand command);
    
    /**
     * Handles the change handle command following the sequence diagram flow.
     * @param command The change handle command
     * @return The updated channel
     */
    Channel changeHandle(ChangeHandleCommand command);
    
    /**
     * Handles the update branding command following the sequence diagram flow.
     * @param command The update branding command
     * @return The updated channel
     */
    Channel updateBranding(UpdateBrandingCommand command);
    
    /**
     * Handles the set member role command following the sequence diagram flow.
     * @param command The set member role command
     */
    void setMemberRole(SetMemberRoleCommand command);
}
