package com.youtube.channelservice.application.saga;

import com.youtube.channelservice.domain.models.Role;
import com.youtube.channelservice.domain.repositories.ChannelMemberRepository;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.domain.services.CacheService;
import com.youtube.channelservice.domain.events.ChannelMemberRoleChanged;

import java.time.Instant;
import java.util.List;

/**
 * Saga for setting member role following the sequence diagram flow.
 * Implements the Saga Pattern with proper authorization and validation.
 */
public class SetMemberRoleSaga implements Saga<Void> {
    
    private final String sagaId;
    private final String channelId;
    private final String actorUserId;
    private final String targetUserId;
    private final Role newRole;
    
    // Dependencies
    private final ChannelMemberRepository memberRepo;
    private final EventPublisher eventPublisher;
    private final CacheService cache;
    
    public SetMemberRoleSaga(String sagaId, String channelId, String actorUserId, 
                             String targetUserId, Role newRole,
                             ChannelMemberRepository memberRepo, EventPublisher eventPublisher,
                             CacheService cache) {
        this.sagaId = sagaId;
        this.channelId = channelId;
        this.actorUserId = actorUserId;
        this.targetUserId = targetUserId;
        this.newRole = newRole;
        this.memberRepo = memberRepo;
        this.eventPublisher = eventPublisher;
        this.cache = cache;
    }
    
    @Override
    public Void execute() throws SagaExecutionException {
        SagaContext context = new SagaContext(sagaId, getSagaType());
        context.put("channelId", channelId);
        context.put("actorUserId", actorUserId);
        context.put("targetUserId", targetUserId);
        context.put("newRole", newRole);
        
        try {
            // Step 1: Authorize and validate
            AuthorizeAndValidateStep authorizeStep = new AuthorizeAndValidateStep();
            authorizeStep.execute(context);
            
            // Step 2: Update role
            UpdateRoleStep updateStep = new UpdateRoleStep();
            updateStep.execute(context);
            
            // Step 3: Publish events and invalidate cache
            PublishRoleChangeEventsStep publishStep = new PublishRoleChangeEventsStep();
            publishStep.execute(context);
            
            return null;
            
        } catch (SagaStepException e) {
            // Compensate executed steps
            compensate(context, e.getStepName());
            throw new SagaExecutionException(sagaId, getSagaType(), e.getStepName(), 
                "Saga execution failed at step: " + e.getStepName(), e);
        }
    }
    
    private void compensate(SagaContext context, String failedStep) {
        String channelId = context.get("channelId", String.class);
        String targetUserId = context.get("targetUserId", String.class);
        Role oldRole = context.get("oldRole", Role.class);
        
        try {
            if ("UPDATE_ROLE".equals(failedStep) || "PUBLISH_ROLE_CHANGE_EVENTS".equals(failedStep)) {
                // Revert role change if we have the old role
                if (oldRole != null) {
                    memberRepo.updateRole(channelId, targetUserId, oldRole);
                } else {
                    // If there was no old role, remove the member
                    memberRepo.remove(channelId, targetUserId);
                }
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
        return "SET_MEMBER_ROLE";
    }
    
    @Override
    public List<SagaStep> getSteps() {
        return List.of(
            new AuthorizeAndValidateStep(),
            new UpdateRoleStep(),
            new PublishRoleChangeEventsStep()
        );
    }
    
    // Inner step classes
    private class AuthorizeAndValidateStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String actorUserId = context.get("actorUserId", String.class);
            String targetUserId = context.get("targetUserId", String.class);
            Role newRole = context.get("newRole", Role.class);
            
            // Only OWNER can change roles
            Role actorRole = memberRepo.roleOf(channelId, actorUserId)
                .orElseThrow(() -> new SagaStepException(getStepName(), sagaId, "ACTOR_NOT_MEMBER"));
            
            if (actorRole != Role.OWNER) {
                throw new SagaStepException(getStepName(), sagaId, "OWNER_REQUIRED");
            }
            
            // Prevent illegal OWNER assignments/demotions via this API
            if (newRole == Role.OWNER && !targetUserId.equals(actorUserId)) {
                throw new SagaStepException(getStepName(), sagaId, "TRANSFER_OWNERSHIP_NOT_SUPPORTED");
            }
            
            // Do not allow changing current OWNER's role via this API
            Role targetCurrentRole = memberRepo.roleOf(channelId, targetUserId).orElse(null);
            if (targetCurrentRole == Role.OWNER && !targetUserId.equals(actorUserId)) {
                throw new SagaStepException(getStepName(), sagaId, "CANNOT_DEMOTE_OWNER");
            }
            
            context.put("oldRole", targetCurrentRole);
            
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
    
    private class UpdateRoleStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String targetUserId = context.get("targetUserId", String.class);
            Role newRole = context.get("newRole", Role.class);
            
            memberRepo.updateRole(channelId, targetUserId, newRole);
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String targetUserId = context.get("targetUserId", String.class);
            Role oldRole = context.get("oldRole", Role.class);
            
            if (oldRole != null) {
                memberRepo.updateRole(channelId, targetUserId, oldRole);
            } else {
                memberRepo.remove(channelId, targetUserId);
            }
        }
        
        @Override
        public String getStepName() {
            return "UPDATE_ROLE";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
    
    private class PublishRoleChangeEventsStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String targetUserId = context.get("targetUserId", String.class);
            Role oldRole = context.get("oldRole", Role.class);
            Role newRole = context.get("newRole", Role.class);
            
            eventPublisher.publishMemberRoleChanged(
                new ChannelMemberRoleChanged(channelId, targetUserId, 
                    oldRole == null ? null : oldRole.name(), 
                    newRole.name())
            );
            
            cache.invalidatePermissions(channelId, targetUserId);
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) throws SagaStepException {
            String channelId = context.get("channelId", String.class);
            String targetUserId = context.get("targetUserId", String.class);
            
            // Invalidate cache
            cache.invalidatePermissions(channelId, targetUserId);
            
            // Event publishing cannot be compensated, but that's acceptable
            // as events are typically idempotent and consumers should handle duplicates
        }
        
        @Override
        public String getStepName() {
            return "PUBLISH_ROLE_CHANGE_EVENTS";
        }
        
        @Override
        public boolean isCompensatable() {
            return true;
        }
    }
}
