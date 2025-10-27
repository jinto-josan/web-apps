package com.youtube.channelservice.application.usecases;

import com.youtube.channelservice.application.commands.CreateChannelCommand;
import com.youtube.channelservice.application.commands.ChangeHandleCommand;
import com.youtube.channelservice.application.commands.UpdateBrandingCommand;
import com.youtube.channelservice.application.commands.SetMemberRoleCommand;
import com.youtube.channelservice.domain.models.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of ChannelUseCase.
 * Orchestrates channel operations using the Command Pattern and Saga Pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelUseCaseImpl implements ChannelUseCase {
    
    private final ChannelCommandHandler commandHandler;
    
    @Override
    public Channel createChannel(CreateChannelCommand command) {
        log.info("Creating channel with handle: {}", command.getHandle());
        return commandHandler.createChannel(command);
    }
    
    @Override
    public Channel changeHandle(ChangeHandleCommand command) {
        log.info("Changing handle for channel: {} to {}", command.getChannelId(), command.getNewHandle());
        return commandHandler.changeHandle(command);
    }
    
    @Override
    public Channel updateBranding(UpdateBrandingCommand command) {
        log.info("Updating branding for channel: {}", command.getChannelId());
        return commandHandler.updateBranding(command);
    }
    
    @Override
    public void setMemberRole(SetMemberRoleCommand command) {
        log.info("Setting role {} for user {} in channel {}", 
                command.getNewRole(), command.getTargetUserId(), command.getChannelId());
        commandHandler.setMemberRole(command);
    }
}
