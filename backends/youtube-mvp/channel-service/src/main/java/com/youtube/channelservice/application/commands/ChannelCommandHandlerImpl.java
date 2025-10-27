package com.youtube.channelservice.application.commands;

import com.youtube.channelservice.application.saga.CreateChannelSaga;
import com.youtube.channelservice.application.saga.ChangeHandleSaga;
import com.youtube.channelservice.application.saga.UpdateBrandingSaga;
import com.youtube.channelservice.application.saga.SetMemberRoleSaga;
import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.models.Branding;
import com.youtube.channelservice.domain.repositories.ChannelRepository;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.domain.repositories.HandleRegistry;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.domain.services.CacheService;
import com.youtube.channelservice.domain.services.BlobUriValidator;
import com.youtube.channelservice.domain.services.ReservedWordsService;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of ChannelCommandHandler using Saga Pattern.
 * Delegates complex operations to saga implementations for better isolation and rollback capability.
 */
public class ChannelCommandHandlerImpl implements ChannelCommandHandler {
    
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(10);
    private static final Duration HANDLE_CHANGE_COOLDOWN = Duration.ofDays(14);
    
    private final ChannelRepository channelRepo;
    private final HandleRegistry handleRegistry;
    private final ChannelMemberRepository memberRepo;
    private final EventPublisher eventPublisher;
    private final CacheService cache;
    private final BlobUriValidator blobUriValidator;
    private final ReservedWordsService reservedWords;
    
    public ChannelCommandHandlerImpl(ChannelRepository channelRepo, HandleRegistry handleRegistry,
                                    ChannelMemberRepository memberRepo, EventPublisher eventPublisher,
                                    CacheService cache, BlobUriValidator blobUriValidator,
                                    ReservedWordsService reservedWords) {
        this.channelRepo = channelRepo;
        this.handleRegistry = handleRegistry;
        this.memberRepo = memberRepo;
        this.eventPublisher = eventPublisher;
        this.cache = cache;
        this.blobUriValidator = blobUriValidator;
        this.reservedWords = reservedWords;
    }
    
    @Override
    public Channel createChannel(CreateChannelCommand command) {
        String sagaId = UUID.randomUUID().toString();
        
        CreateChannelSaga saga = new CreateChannelSaga(
            sagaId,
            command.getCommandId(), // Using command ID as ULID
            command.getOwnerUserId(),
            command.getHandle(),
            command.getTitle(),
            command.getDescription(),
            command.getLanguage(),
            command.getCountry(),
            channelRepo,
            handleRegistry,
            memberRepo,
            eventPublisher,
            cache,
            reservedWords
        );
        
        try {
            return saga.execute();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create channel", e);
        }
    }
    
    @Override
    public Channel changeHandle(ChangeHandleCommand command) {
        String sagaId = UUID.randomUUID().toString();
        
        ChangeHandleSaga saga = new ChangeHandleSaga(
            sagaId,
            command.getChannelId(),
            command.getActorUserId(),
            command.getNewHandle(),
            command.getIfMatchEtag(),
            command.getLastChangeAt(),
            channelRepo,
            handleRegistry,
            memberRepo,
            eventPublisher,
            cache,
            reservedWords
        );
        
        try {
            return saga.execute();
        } catch (Exception e) {
            throw new RuntimeException("Failed to change handle", e);
        }
    }
    
    @Override
    public Channel updateBranding(UpdateBrandingCommand command) {
        String sagaId = UUID.randomUUID().toString();
        
        UpdateBrandingSaga saga = new UpdateBrandingSaga(
            sagaId,
            command.getChannelId(),
            command.getActorUserId(),
            command.getBranding(),
            command.getIfMatchEtag(),
            channelRepo,
            memberRepo,
            eventPublisher,
            blobUriValidator
        );
        
        try {
            return saga.execute();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update branding", e);
        }
    }
    
    @Override
    public void setMemberRole(SetMemberRoleCommand command) {
        String sagaId = UUID.randomUUID().toString();
        
        SetMemberRoleSaga saga = new SetMemberRoleSaga(
            sagaId,
            command.getChannelId(),
            command.getActorUserId(),
            command.getTargetUserId(),
            command.getNewRole(),
            memberRepo,
            eventPublisher,
            cache
        );
        
        try {
            saga.execute();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set member role", e);
        }
    }
}
