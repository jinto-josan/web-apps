package com.youtube.userprofileservice.application.saga;

import com.youtube.userprofileservice.application.commands.UpdateProfileCommand;
import com.youtube.userprofileservice.domain.entities.AccountProfile;
import com.youtube.userprofileservice.domain.events.ProfileUpdated;
import com.youtube.userprofileservice.domain.repositories.ProfileRepository;
import com.youtube.userprofileservice.domain.services.EventPublisher;
import com.youtube.userprofileservice.domain.services.CacheService;
import com.youtube.userprofileservice.domain.services.BlobUriValidator;
import com.youtube.common.domain.error.ConflictException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saga for updating a user's profile.
 * Implements the Saga Pattern with compensation for rollback scenarios.
 */
public class UpdateProfileSaga implements Saga<AccountProfile> {
    
    private final String sagaId;
    private final UpdateProfileCommand command;
    private final String updatedBy;
    
    // Dependencies
    private final ProfileRepository profileRepository;
    private final EventPublisher eventPublisher;
    private final CacheService cacheService;
    private final BlobUriValidator blobUriValidator;
    
    public UpdateProfileSaga(String sagaId, UpdateProfileCommand command, String updatedBy,
                            ProfileRepository profileRepository, EventPublisher eventPublisher,
                            CacheService cacheService, BlobUriValidator blobUriValidator) {
        this.sagaId = sagaId;
        this.command = command;
        this.updatedBy = updatedBy;
        this.profileRepository = profileRepository;
        this.eventPublisher = eventPublisher;
        this.cacheService = cacheService;
        this.blobUriValidator = blobUriValidator;
    }
    
    @Override
    public AccountProfile execute() throws SagaExecutionException {
        SagaContext context = new SagaContext(sagaId, getSagaType());
        context.put("command", command);
        context.put("updatedBy", updatedBy);
        
        try {
            // Step 1: Load and validate profile
            LoadProfileStep loadStep = new LoadProfileStep();
            AccountProfile profile = (AccountProfile) loadStep.execute(context);
            context.put("originalProfile", profile);
            
            // Step 2: Validate photo URL if provided
            if (command.getPhotoUrl() != null) {
                ValidatePhotoUrlStep validateStep = new ValidatePhotoUrlStep();
                validateStep.execute(context);
            }
            
            // Step 3: Update profile
            UpdateProfileStep updateStep = new UpdateProfileStep();
            AccountProfile updated = (AccountProfile) updateStep.execute(context);
            context.put("updatedProfile", updated);
            
            // Step 4: Invalidate cache and publish events
            PublishEventsStep publishStep = new PublishEventsStep();
            publishStep.execute(context);
            
            return updated;
            
        } catch (SagaStepException e) {
            compensate(context, e.getStepName());
            throw new SagaExecutionException(sagaId, getSagaType(), e.getStepName(),
                "Saga execution failed at step: " + e.getStepName(), e);
        }
    }
    
    private void compensate(SagaContext context, String failedStep) {
        // Most steps don't need compensation as they are idempotent
        // Only cache operations might need compensation
        try {
            if ("PUBLISH_EVENTS".equals(failedStep)) {
                // Invalidate cache since we updated but failed to publish
                cacheService.invalidateProfile(command.getAccountId());
            }
        } catch (Exception e) {
            // Log but don't throw
            System.err.println("Compensation failed for saga " + sagaId + ": " + e.getMessage());
        }
    }
    
    @Override
    public String getSagaId() {
        return sagaId;
    }
    
    @Override
    public String getSagaType() {
        return "UPDATE_PROFILE";
    }
    
    @Override
    public List<SagaStep> getSteps() {
        return Arrays.asList(
            new LoadProfileStep(),
            new ValidatePhotoUrlStep(),
            new UpdateProfileStep(),
            new PublishEventsStep()
        );
    }
    
    // Inner step classes
    private class LoadProfileStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            UpdateProfileCommand cmd = context.get("command", UpdateProfileCommand.class);
            
            AccountProfile profile = profileRepository.findByAccountId(cmd.getAccountId())
                .orElseThrow(() -> new SagaStepException(getStepName(), sagaId, "PROFILE_NOT_FOUND"));
            
            // Check ETag for optimistic locking
            if (cmd.getEtag() != null && !cmd.getEtag().equals(profile.getEtag())) {
                throw new SagaStepException(getStepName(), sagaId, "ETAG_MISMATCH");
            }
            
            return profile;
        }
        
        @Override
        public void compensate(SagaContext context) {
            // No compensation needed
        }
        
        @Override
        public String getStepName() {
            return "LOAD_PROFILE";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
    
    private class ValidatePhotoUrlStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            UpdateProfileCommand cmd = context.get("command", UpdateProfileCommand.class);
            
            if (cmd.getPhotoUrl() != null && !blobUriValidator.isValid(cmd.getPhotoUrl())) {
                throw new SagaStepException(getStepName(), sagaId, "INVALID_PHOTO_URL");
            }
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) {
            // No compensation needed
        }
        
        @Override
        public String getStepName() {
            return "VALIDATE_PHOTO_URL";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
    
    private class UpdateProfileStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            UpdateProfileCommand cmd = context.get("command", UpdateProfileCommand.class);
            AccountProfile original = context.get("originalProfile", AccountProfile.class);
            String updatedBy = context.get("updatedBy", String.class);
            
            Instant now = Instant.now();
            
            // Determine which fields changed
            List<String> fieldsChanged = Arrays.asList();
            if (cmd.getDisplayName() != null && !cmd.getDisplayName().equals(original.getDisplayName())) {
                fieldsChanged.add("displayName");
            }
            if (cmd.getPhotoUrl() != null && !cmd.getPhotoUrl().equals(original.getPhotoUrl())) {
                fieldsChanged.add("photoUrl");
            }
            if (cmd.getLocale() != null && !cmd.getLocale().equals(original.getLocale())) {
                fieldsChanged.add("locale");
            }
            if (cmd.getCountry() != null && !cmd.getCountry().equals(original.getCountry())) {
                fieldsChanged.add("country");
            }
            if (cmd.getTimezone() != null && !cmd.getTimezone().equals(original.getTimezone())) {
                fieldsChanged.add("timezone");
            }
            
            context.put("fieldsChanged", fieldsChanged.stream().collect(Collectors.joining(",")));
            
            // Build updated profile
            AccountProfile.AccountProfileBuilder builder = original.toBuilder()
                    .version(original.getVersion() + 1)
                    .updatedAt(now)
                    .updatedBy(updatedBy);
            
            if (cmd.getDisplayName() != null) {
                builder.displayName(cmd.getDisplayName());
            }
            if (cmd.getPhotoUrl() != null) {
                builder.photoUrl(blobUriValidator.normalize(cmd.getPhotoUrl()));
            }
            if (cmd.getLocale() != null) {
                builder.locale(cmd.getLocale());
            }
            if (cmd.getCountry() != null) {
                builder.country(cmd.getCountry());
            }
            if (cmd.getTimezone() != null) {
                builder.timezone(cmd.getTimezone());
            }
            
            // Generate new ETag
            String newEtag = String.valueOf(System.currentTimeMillis());
            builder.etag(newEtag);
            
            AccountProfile updated = builder.build();
            
            try {
                AccountProfile saved = profileRepository.update(updated);
                return saved;
            } catch (ConflictException e) {
                throw new SagaStepException(getStepName(), sagaId, "UPDATE_CONFLICT", e);
            }
        }
        
        @Override
        public void compensate(SagaContext context) {
            // No compensation - profile updates are idempotent
        }
        
        @Override
        public String getStepName() {
            return "UPDATE_PROFILE";
        }
        
        @Override
        public boolean isCompensatable() {
            return false;
        }
    }
    
    private class PublishEventsStep implements SagaStep {
        @Override
        public Object execute(SagaContext context) throws SagaStepException {
            AccountProfile updated = context.get("updatedProfile", AccountProfile.class);
            String updatedBy = context.get("updatedBy", String.class);
            String fieldsChanged = context.get("fieldsChanged", String.class);
            
            // Publish profile updated event
            ProfileUpdated event = new ProfileUpdated(
                updated.getAccountId(),
                updatedBy,
                fieldsChanged
            );
            eventPublisher.publishProfileUpdated(event);
            
            // Invalidate cache
            cacheService.invalidateProfile(updated.getAccountId());
            
            return null;
        }
        
        @Override
        public void compensate(SagaContext context) {
            // Events are idempotent, no compensation needed
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

