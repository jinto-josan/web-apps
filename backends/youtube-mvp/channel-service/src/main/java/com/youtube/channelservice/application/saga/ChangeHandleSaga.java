package com.youtube.channelservice.application.saga;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.repositories.ChannelRepository;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.domain.repositories.HandleRegistry;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.domain.services.CacheService;
import com.youtube.channelservice.domain.services.ReservedWordsService;
import com.youtube.channelservice.domain.events.ChannelHandleChanged;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Saga for changing a channel handle following the sequence diagram flow.
 * Implements the Saga Pattern with proper compensation for rollback scenarios.
 */
public class ChangeHandleSaga implements Saga<Channel> {
    
    private static final Pattern HANDLE_PATTERN = Pattern.compile("^[a-z0-9._-]{3,30}$");
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(10);
    private static final Duration HANDLE_CHANGE_COOLDOWN = Duration.ofDays(14);
    
    private final String sagaId;
    private final String channelId;
    private final String actorUserId;
    private final String newHandle;
    private final String ifMatchEtag;
    private final Instant lastChangeAt;
    
    // Dependencies
    private final ChannelRepository channelRepo;
    private final HandleRegistry handleRegistry;
    private final ChannelMemberRepository memberRepo;
    private final EventPublisher eventPublisher;
    private final CacheService cache;
    private final ReservedWordsService reservedWords;
    
    public ChangeHandleSaga(String sagaId, String channelId, String actorUserId, String newHandle,
                            String ifMatchEtag, Instant lastChangeAt,
                            ChannelRepository channelRepo, HandleRegistry handleRegistry,
                            ChannelMemberRepository memberRepo, EventPublisher eventPublisher,
                            CacheService cache, ReservedWordsService reservedWords) {
        this.sagaId = sagaId;
        this.channelId = channelId;
        this.actorUserId = actorUserId;
        this.newHandle = newHandle;
        this.ifMatchEtag = ifMatchEtag;
        this.lastChangeAt = lastChangeAt;
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
        context.put("channelId", channelId);
        context.put("actorUserId", actorUserId);
        context.put("newHandleLower", newHandle.toLowerCase());
        context.put("ifMatchEtag", ifMatchEtag);
        
        try {
            // Step 1: Authorize and validate
            AuthorizeAndValidateStep authorizeStep = new AuthorizeAndValidateStep();
            authorizeStep.execute(context);
            
            // Step 2: Reserve new handle
            ReserveNewHandleStep reserveStep = new ReserveNewHandleStep();
            reserveStep.execute(context);
            
            // Step 3: Update channel handle
            UpdateChannelHandleStep updateStep = new UpdateChannelHandleStep();
            Channel updatedChannel = (Channel) updateStep.execute(context);
            
            // Step 4: Commit new handle
            CommitNewHandleStep commitStep = new CommitNewHandleStep();
            commitStep.execute(context);
            
            // Step 5: Release old handle and update cache
            ReleaseOldHandleStep releaseStep = new ReleaseOldHandleStep();
            releaseStep.execute(context);
            
            // Step 6: Publish events
            PublishHandleChangeEventsStep publishStep = new PublishHandleChangeEventsStep();
            publishStep.execute(context);
            
            return updatedChannel;
            
        } catch (SagaStepException e) {
            // Compensate executed steps
            compensate(context, e.getStepName());
            throw new SagaExecutionException(sagaId, getSagaType(), e.getStepName(), 
                "Saga execution failed at step: " + e.getStepName(), e);
        }
    }
    
    private void compensate(SagaContext context, String failedStep) {
        String newHandleLower = context.get("newHandleLower", String.class);
        String oldHandleLower = context.get("oldHandleLower", String.class);
        
        try {
            if ("COMMIT_NEW_HANDLE".equals(failedStep) || "RELEASE_OLD_HANDLE".equals(failedStep) || 
                "PUBLISH_HANDLE_CHANGE_EVENTS".equals(failedStep)) {
                // Revert channel handle and release new handle
                String channelId = context.get("channelId", String.class);
                String oldEtag = context.get("oldEtag", String.class);
                if (channelId != null && oldHandleLower != null && oldEtag != null) {
                    channelRepo.updateHandle(channelId, newHandleLower, oldHandleLower, 
                        oldEtag, context.get("oldVersion", Integer.class) + 1, Instant.now());
                }
                handleRegistry.release(newHandleLower);
            } else if ("UPDATE_CHANNEL_HANDLE".equals(failedStep)) {
                // Only release new handle
                handleRegistry.release(newHandleLower);
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
        return "CHANGE_HANDLE";
    }
    
    @Override
    public List<SagaStep> getSteps() {
        return List.of(
            new AuthorizeAndValidateStep(),
            new ReserveNewHandleStep(),
            new UpdateChannelHandleStep(),
            new CommitNewHandleStep(),
            new ReleaseOldHandleStep(),
            new PublishHandleChangeEventsStep()
        );
    }
    
    // Inner step classes
    private class AuthorizeAndValidateStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String actorUserId = context.get("actorUserId", String.class);
            String newHandleLower = context.get("newHandleLower", String.class);
            
            // Check cooldown
            if (lastChangeAt != null && Instant.now().isBefore(lastChangeAt.plus(HANDLE_CHANGE_COOLDOWN))) {
                throw new SagaStepException(getStepName(), sagaId, "HANDLE_CHANGE_COOLDOWN");
            }
            
            // Validate handle format
            if (!HANDLE_PATTERN.matcher(newHandleLower).matches()) {
                throw new SagaStepException(getStepName(), sagaId, "INVALID_HANDLE");
            }
            
            // Check if handle is reserved
            if (reservedWords.isReserved(newHandleLower)) {
                throw new SagaStepException(getStepName(), sagaId, "HANDLE_RESERVED");
            }
            
            // Authorize owner
            Role actorRole = memberRepo.roleOf(channelId, actorUserId)
                .orElseThrow(() -> new SagaStepException(getStepName(), sagaId, "USER_NOT_MEMBER"));
            
            if (actorRole != Role.OWNER) {
                throw new SagaStepException(getStepName(), sagaId, "OWNER_REQUIRED");
            }
            
            // Get existing channel
            Channel existing = channelRepo.findById(channelId)
                .orElseThrow(() -> new SagaStepException(getStepName(), sagaId, "CHANNEL_NOT_FOUND"));
            
            context.put("existingChannel", existing);
            context.put("oldHandleLower", existing.handleLower());
            context.put("oldEtag", existing.etag());
            context.put("oldVersion", existing.version());
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // No compensation needed for validation
        }
        
        @Override
        public String getStepName() {
            return "AUTHORIZE_AND_VALIDATE";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
    
    private class ReserveNewHandleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String newHandleLower = context.get("newHandleLower", String.class);
            String actorUserId = context.get("actorUserId", String.class);
            
            boolean reserved = handleRegistry.reserve(newHandleLower, actorUserId, RESERVATION_TTL);
            if (!reserved) {
                throw new SagaStepException(getStepName(), sagaId, "HANDLE_TAKEN");
            }
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String newHandleLower = context.get("newHandleLower", String.class);
            handleRegistry.release(newHandleLower);
        }
        
        @Override
        public String getStepName() {
            return "RESERVE_NEW_HANDLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class UpdateChannelHandleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String oldHandleLower = context.get("oldHandleLower", String.class);
            String newHandleLower = context.get("newHandleLower", String.class);
            String ifMatchEtag = context.get("ifMatchEtag", String.class);
            Integer oldVersion = context.get("oldVersion", Integer.class);
            
            Channel updated = channelRepo.updateHandle(channelId, oldHandleLower, newHandleLower, 
                ifMatchEtag, oldVersion + 1, Instant.now());
            
            context.put("updatedChannel", updated);
            return updated;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String oldHandleLower = context.get("oldHandleLower", String.class);
            String newHandleLower = context.get("newHandleLower", String.class);
            String oldEtag = context.get("oldEtag", String.class);
            Integer oldVersion = context.get("oldVersion", Integer.class);
            
            // Revert the handle change
            channelRepo.updateHandle(channelId, newHandleLower, oldHandleLower, 
                oldEtag, oldVersion + 1, Instant.now());
        }
        
        @Override
        public String getStepName() {
            return "UPDATE_CHANNEL_HANDLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class CommitNewHandleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String newHandleLower = context.get("newHandleLower", String.class);
            String channelId = context.get("channelId", String.class);
            
            boolean committed = handleRegistry.commit(newHandleLower, channelId);
            if (!committed) {
                throw new SagaStepException(getStepName(), sagaId, "HANDLE_COMMIT_FAILED");
            }
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String newHandleLower = context.get("newHandleLower", String.class);
            handleRegistry.release(newHandleLower);
        }
        
        @Override
        public String getStepName() {
            return "COMMIT_NEW_HANDLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class ReleaseOldHandleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String oldHandleLower = context.get("oldHandleLower", String.class);
            String newHandleLower = context.get("newHandleLower", String.class);
            String channelId = context.get("channelId", String.class);
            
            // Release old handle
            handleRegistry.release(oldHandleLower);
            
            // Update cache
            cache.putHandleMapping(newHandleLower, channelId);
            cache.invalidateHandleMapping(oldHandleLower);
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String oldHandleLower = context.get("oldHandleLower", String.class);
            String newHandleLower = context.get("newHandleLower", String.class);
            String channelId = context.get("channelId", String.class);
            
            // Revert cache changes
            cache.putHandleMapping(oldHandleLower, channelId);
            cache.invalidateHandleMapping(newHandleLower);
        }
        
        @Override
        public String getStepName() {
            return "RELEASE_OLD_HANDLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class PublishHandleChangeEventsStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String oldHandleLower = context.get("oldHandleLower", String.class);
            String newHandleLower = context.get("newHandleLower", String.class);
            
            eventPublisher.publishChannelHandleChanged(
                new ChannelHandleChanged(channelId, oldHandleLower, newHandleLower)
            );
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // Event publishing cannot be compensated, but that's acceptable
            // as events are typically idempotent and consumers should handle duplicates
        }
        
        @Override
        public String getStepName() {
            return "PUBLISH_HANDLE_CHANGE_EVENTS";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
}
