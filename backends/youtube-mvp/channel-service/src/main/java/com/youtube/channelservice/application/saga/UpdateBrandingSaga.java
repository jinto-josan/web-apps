package com.youtube.channelservice.application.saga;

import com.youtube.channelservice.domain.models.Channel;
import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.models.Branding;
import com.youtube.channelservice.domain.repositories.ChannelRepository;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.domain.services.BlobUriValidator;

import java.util.List;

/**
 * Saga for updating channel branding following the sequence diagram flow.
 * Implements the Saga Pattern with proper validation and ETag handling.
 */
public class UpdateBrandingSaga implements Saga<Channel> {
    
    private final String sagaId;
    private final String channelId;
    private final String actorUserId;
    private final Branding branding;
    private final String ifMatchEtag;
    
    // Dependencies
    private final ChannelRepository channelRepo;
    private final ChannelMemberRepository memberRepo;
    private final EventPublisher eventPublisher;
    private final BlobUriValidator blobUriValidator;
    
    public UpdateBrandingSaga(String sagaId, String channelId, String actorUserId, Branding branding,
                              String ifMatchEtag, ChannelRepository channelRepo,
                              ChannelMemberRepository memberRepo, EventPublisher eventPublisher,
                              BlobUriValidator blobUriValidator) {
        this.sagaId = sagaId;
        this.channelId = channelId;
        this.actorUserId = actorUserId;
        this.branding = branding;
        this.ifMatchEtag = ifMatchEtag;
        this.channelRepo = channelRepo;
        this.memberRepo = memberRepo;
        this.eventPublisher = eventPublisher;
        this.blobUriValidator = blobUriValidator;
    }
    
    @Override
    public Channel execute() throws SagaExecutionException {
        SagaContext context = new SagaContext(sagaId, getSagaType());
        context.put("channelId", channelId);
        context.put("actorUserId", actorUserId);
        context.put("branding", branding);
        context.put("ifMatchEtag", ifMatchEtag);
        
        try {
            // Step 1: Authorize and validate
            AuthorizeAndValidateStep authorizeStep = new AuthorizeAndValidateStep();
            authorizeStep.execute(context);
            
            // Step 2: Update branding
            UpdateBrandingStep updateStep = new UpdateBrandingStep();
            Channel updatedChannel = (Channel) updateStep.execute(context);
            
            // Step 3: Publish events
            PublishBrandingUpdateEventsStep publishStep = new PublishBrandingUpdateEventsStep();
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
        // For branding updates, compensation is minimal since it's mostly validation
        // and the update is atomic with ETag checking
        try {
            if ("UPDATE_BRANDING".equals(failedStep)) {
                // Could potentially revert to previous branding, but this is complex
                // and branding updates are typically not critical enough to warrant complex compensation
                System.err.println("Branding update compensation not implemented for saga " + sagaId);
            }
        } catch (Exception e) {
            System.err.println("Compensation failed for saga " + sagaId + ": " + e.getMessage());
        }
    }
    
    @Override
    public String getSagaId() {
        return sagaId;
    }
    
    @Override
    public String getSagaType() {
        return "UPDATE_BRANDING";
    }
    
    @Override
    public List<SagaStep> getSteps() {
        return List.of(
            new AuthorizeAndValidateStep(),
            new UpdateBrandingStep(),
            new PublishBrandingUpdateEventsStep()
        );
    }
    
    // Inner step classes
    private class AuthorizeAndValidateStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String actorUserId = context.get("actorUserId", String.class);
            Branding branding = context.get("branding", Branding.class);
            
            // Authorize MANAGER or OWNER
            Role actorRole = memberRepo.roleOf(channelId, actorUserId)
                .orElseThrow(() -> new SagaStepException(getStepName(), sagaId, "USER_NOT_MEMBER"));
            
            if (actorRole != Role.OWNER && actorRole != Role.MANAGER) {
                throw new SagaStepException(getStepName(), sagaId, "MANAGER_OR_OWNER_REQUIRED");
            }
            
            // Validate URIs
            blobUriValidator.validate(branding.avatarUri());
            blobUriValidator.validate(branding.bannerUri());
            
            // Get existing channel
            Channel existing = channelRepo.findById(channelId)
                .orElseThrow(() -> new SagaStepException(getStepName(), sagaId, "CHANNEL_NOT_FOUND"));
            
            context.put("existingChannel", existing);
            
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
    
    private class UpdateBrandingStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            Channel existing = context.get("existingChannel", Channel.class);
            Branding branding = context.get("branding", Branding.class);
            String ifMatchEtag = context.get("ifMatchEtag", String.class);
            
            Channel updated = channelRepo.updateBranding(existing, branding, ifMatchEtag);
            context.put("updatedChannel", updated);
            
            return updated;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // Branding updates are atomic with ETag checking
            // Compensation would require storing previous state, which is complex
            // For now, we rely on the ETag mechanism to prevent conflicts
        }
        
        @Override
        public String getStepName() {
            return "UPDATE_BRANDING";
        }
        
        @Override
        public boolean isCompensatable() {
            return false; // Complex to implement properly
        }
    }
    
    private class PublishBrandingUpdateEventsStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            
            eventPublisher.publishChannelUpdated(channelId, "branding");
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            // Event publishing cannot be compensated, but that's acceptable
            // as events are typically idempotent and consumers should handle duplicates
        }
        
        @Override
        public String getStepName() {
            return "PUBLISH_BRANDING_UPDATE_EVENTS";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
}
