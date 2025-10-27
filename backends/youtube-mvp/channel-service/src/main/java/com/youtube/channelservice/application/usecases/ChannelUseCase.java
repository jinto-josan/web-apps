package com.youtube.channelservice.application.usecases;

import com.youtube.channelservice.application.commands.CreateChannelCommand;
import com.youtube.channelservice.application.commands.ChangeHandleCommand;
import com.youtube.channelservice.application.commands.UpdateBrandingCommand;
import com.youtube.channelservice.application.commands.SetMemberRoleCommand;
import com.youtube.channelservice.domain.models.Channel;

/**
 * Use case interface for channel management operations.
 * Defines the application layer contract for channel operations.
 */
public interface ChannelUseCase {
    
    /**
     * Creates a new channel.
     * @param command The create channel command
     * @return The created channel
     */
    Channel createChannel(CreateChannelCommand command);
    
    /**
     * Changes a channel's handle.
     * @param command The change handle command
     * @return The updated channel
     */
    Channel changeHandle(ChangeHandleCommand command);
    
    /**
     * Updates a channel's branding.
     * @param command The update branding command
     * @return The updated channel
     */
    Channel updateBranding(UpdateBrandingCommand command);
    
    /**
     * Sets a member's role in a channel.
     * @param command The set member role command
     */
    void setMemberRole(SetMemberRoleCommand command);
}
