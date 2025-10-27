package com.youtube.channelservice.application.saga;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.repositories.ChannelRepository;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.domain.repositories.HandleRegistry;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.domain.services.CacheService;
import com.youtube.channelservice.domain.services.ReservedWordsService;
import com.youtube.channelservice.domain.events.ChannelCreated;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Saga for creating a channel following the sequence diagram flow.
 * Implements the Saga Pattern with proper compensation for rollback scenarios.
 */
public class CreateChannelSaga implements Saga<Channel> {
    
    private static final Pattern HANDLE_PATTERN = Pattern.compile("^[a-z0-9._-]{3,30}$");
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(10);
    
    private final String sagaId;
    private final String ulid;
    private final String ownerUserId;
    private final String handle;
    private final String title;
    private final String description;
    private final String language;
    private final String country;
    
    // Dependencies
    private final ChannelRepository channelRepo;
    private final HandleRegistry handleRegistry;
    private final ChannelMemberRepository memberRepo;
    private final EventPublisher eventPublisher;
    private final CacheService cache;
    private final ReservedWordsService reservedWords;
    
    public CreateChannelSaga(String sagaId, String ulid, String ownerUserId, String handle, 
                            String title, String description, String language, String country,
                            ChannelRepository channelRepo, HandleRegistry handleRegistry,
                            ChannelMemberRepository memberRepo, EventPublisher eventPublisher,
                            CacheService cache, ReservedWordsService reservedWords) {
        this.sagaId = sagaId;
        this.ulid = ulid;
        this.ownerUserId = ownerUserId;
        this.handle = handle;
        this.title = title;
        this.description = description;
        this.language = language;
        this.country = country;
        this.channelRepo = channelRepo;
        this.handleRegistry = handleRegistry;
        this.memberRepo = memberRepo;
        this.eventPublisher = eventPublisher;
        this.cache = cache;
        this.reservedWords = reservedWords;
    }
    
    @Override
    public Channel execute() throws SagaExecutionException {
        SagaContext context = new SagaContext(sagaId, getSagaType());
        context.put("handleLower", handle.toLowerCase());
        context.put("ownerUserId", ownerUserId);
        
        try {
            // Step 1: Validate handle
            ValidateHandleStep validateStep = new ValidateHandleStep();
            validateStep.execute(context);
            
            // Step 2: Reserve handle
            ReserveHandleStep reserveStep = new ReserveHandleStep();
            reserveStep.execute(context);
            
            // Step 3: Create channel
            CreateChannelStep createStep = new CreateChannelStep();
            Channel channel = (Channel) createStep.execute(context);
            
            // Step 4: Commit handle
            CommitHandleStep commitStep = new CommitHandleStep();
            commitStep.execute(context);
            
            // Step 5: Publish events and update cache
            PublishEventsStep publishStep = new PublishEventsStep();
            publishStep.execute(context);
            
            return channel;
            
        } catch (SagaStepException e) {
            // Compensate executed steps
            compensate(context, e.getStepName());
            throw new SagaExecutionException(sagaId, getSagaType(), e.getStepName(), 
                "Saga execution failed at step: " + e.getStepName(), e);
        }
    }
    
    private void compensate(SagaContext context, String failedStep) {
        // Compensation logic based on which step failed
        String handleLower = context.get("handleLower", String.class);
        
        try {
            if ("COMMIT_HANDLE".equals(failedStep) || "PUBLISH_EVENTS".equals(failedStep)) {
                // Release handle and delete channel
                handleRegistry.release(handleLower);
                String channelId = context.get("channelId", String.class);
                if (channelId != null) {
                    channelRepo.delete(channelId);
                }
            } else if ("CREATE_CHANNEL".equals(failedStep)) {
                // Only release handle
                handleRegistry.release(handleLower);
            }
        } catch (Exception e) {
            // Log compensation failure but don't throw
            System.err.println("Compensation failed for saga " + sagaId + ": " + e.getMessage());
        }
    }
    
    @Override
    public String getSagaId() {
        return sagaId;
    }
    
    @Override
    public String getSagaType() {
        return "CREATE_CHANNEL";
    }
    
    @Override
    public List<SagaStep> getSteps() {
        return List.of(
            new ValidateHandleStep(),
            new ReserveHandleStep(),
            new CreateChannelStep(),
            new CommitHandleStep(),
            new PublishEventsStep()
        );
    }
    
    // Inner step classes
    private class ValidateHandleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String handleLower = context.get("handleLower", String.class);
            
            if (!HANDLE_PATTERN.matcher(handleLower).matches()) {
                throw new SagaStepException(getStepName(), sagaId, "INVALID_HANDLE");
            }
            
            if (reservedWords.isReserved(handleLower)) {
                throw new SagaStepException(getStepName(), sagaId, "HANDLE_RESERVED");
            }
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // No compensation needed for validation
        }
        
        @Override
        public String getStepName() {
            return "VALIDATE_HANDLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
    
    private class ReserveHandleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String handleLower = context.get("handleLower", String.class);
            String ownerUserId = context.get("ownerUserId", String.class);
            
            boolean reserved = handleRegistry.reserve(handleLower, ownerUserId, RESERVATION_TTL);
            if (!reserved) {
                throw new SagaStepException(getStepName(), sagaId, "HANDLE_TAKEN");
            }
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String handleLower = context.get("handleLower", String.class);
            handleRegistry.release(handleLower);
        }
        
        @Override
        public String getStepName() {
            return "RESERVE_HANDLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class CreateChannelStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String handleLower = context.get("handleLower", String.class);
            String ownerUserId = context.get("ownerUserId", String.class);
            
            Instant now = Instant.now();
            Channel channel = Channel.builder()
                .id(ulid)
                .ownerUserId(ownerUserId)
                .handleLower(handleLower)
                .title(title)
                .description(description)
                .language(language)
                .country(country)
                .version(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
            
            Channel saved = channelRepo.saveNew(channel);
            context.put("channelId", saved.id());
            context.put("channel", saved);
            
            return saved;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            if (channelId != null) {
                channelRepo.delete(channelId);
            }
        }
        
        @Override
        public String getStepName() {
            return "CREATE_CHANNEL";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class CommitHandleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String handleLower = context.get("handleLower", String.class);
            String channelId = context.get("channelId", String.class);
            
            boolean committed = handleRegistry.commit(handleLower, channelId);
            if (!committed) {
                throw new SagaStepException(getStepName(), sagaId, "HANDLE_COMMIT_FAILED");
            }
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String handleLower = context.get("handleLower", String.class);
            handleRegistry.release(handleLower);
        }
        
        @Override
        public String getStepName() {
            return "COMMIT_HANDLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class PublishEventsStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            Channel channel = context.get("channel", Channel.class);
            String handleLower = context.get("handleLower", String.class);
            
            // Publish channel created event
            eventPublisher.publishChannelCreated(
                new ChannelCreated(channel.id(), channel.ownerUserId(), handleLower)
            );
            
            // Update cache
            cache.putHandleMapping(handleLower, channel.id());
            
            // Add owner as member
            memberRepo.add(channel.id(), channel.ownerUserId(), Role.OWNER);
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String handleLower = context.get("handleLower", String.class);
            String channelId = context.get("channelId", String.class);
            
            // Invalidate cache
            cache.invalidateHandleMapping(handleLower);
            
            // Note: Event publishing cannot be compensated, but that's acceptable
            // as events are typically idempotent and consumers should handle duplicates
        }
        
        @Override
        public String getStepName() {
            return "PUBLISH_EVENTS";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
}
