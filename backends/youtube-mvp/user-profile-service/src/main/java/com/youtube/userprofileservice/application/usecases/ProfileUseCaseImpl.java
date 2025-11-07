package com.youtube.userprofileservice.application.usecases;

import com.youtube.common.domain.core.Clock;
import com.youtube.common.domain.core.UnitOfWork;
import com.youtube.common.domain.events.EventPublisher;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.application.commands.*;
import com.youtube.userprofileservice.application.queries.*;
import com.youtube.userprofileservice.domain.entities.*;
import com.youtube.userprofileservice.domain.events.*;
import com.youtube.userprofileservice.domain.repositories.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ProfileUseCase.
 * Uses UnitOfWork for transaction management and EventPublisher for event publishing.
 */
@Slf4j
@Service
public class ProfileUseCaseImpl implements ProfileUseCase {
    
    private final ProfileRepository profileRepository;
    private final EventPublisher eventPublisher;
    private final UnitOfWork unitOfWork;
    private final Clock clock;
    
    public ProfileUseCaseImpl(
            ProfileRepository profileRepository,
            EventPublisher eventPublisher,
            UnitOfWork unitOfWork,
            Clock clock) {
        this.profileRepository = profileRepository;
        this.eventPublisher = eventPublisher;
        this.unitOfWork = unitOfWork;
        this.clock = clock;
    }
    
    @Override
    @Transactional
    public AccountProfile updateProfile(UpdateProfileCommand command, String updatedBy) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Updating profile - accountId: {}, updatedBy: {}, correlationId: {}", 
                command.getAccountId(), updatedBy, correlationId);
        
        unitOfWork.begin();
        try {
            AccountProfile profile = profileRepository.findByAccountId(command.getAccountId())
                    .orElseThrow(() -> {
                        log.warn("Profile not found - accountId: {}, correlationId: {}", 
                                command.getAccountId(), correlationId);
                        return new IllegalArgumentException("Profile not found: " + command.getAccountId());
                    });
            
            // Check ETag for optimistic locking
            if (command.getEtag() != null && !command.getEtag().equals(profile.getEtag())) {
                log.warn("ETag mismatch - accountId: {}, expected: {}, actual: {}, correlationId: {}", 
                        command.getAccountId(), command.getEtag(), profile.getEtag(), correlationId);
                throw new com.youtube.common.domain.ConflictException("ETag mismatch");
            }
            
            Instant now = clock.now();
            List<String> changedFields = new ArrayList<>();
            
            AccountProfile.AccountProfileBuilder builder = profile.toBuilder();
            
            if (command.getDisplayName() != null && !command.getDisplayName().equals(profile.getDisplayName())) {
                builder.displayName(command.getDisplayName());
                changedFields.add("displayName");
            }
            if (command.getPhotoUrl() != null && !command.getPhotoUrl().equals(profile.getPhotoUrl())) {
                builder.photoUrl(command.getPhotoUrl());
                changedFields.add("photoUrl");
            }
            if (command.getLocale() != null && !command.getLocale().equals(profile.getLocale())) {
                builder.locale(command.getLocale());
                changedFields.add("locale");
            }
            if (command.getCountry() != null && !command.getCountry().equals(profile.getCountry())) {
                builder.country(command.getCountry());
                changedFields.add("country");
            }
            if (command.getTimezone() != null && !command.getTimezone().equals(profile.getTimezone())) {
                builder.timezone(command.getTimezone());
                changedFields.add("timezone");
            }
            
            AccountProfile updated = builder
                    .version(profile.getVersion() + 1)
                    .updatedAt(now)
                    .updatedBy(updatedBy)
                    .etag(generateEtag(profile.getVersion() + 1, now))
                    .build();
            
            AccountProfile saved = profileRepository.update(updated);
            
            // Publish domain event
            if (!changedFields.isEmpty()) {
                ProfileUpdated event = new ProfileUpdated(
                        saved.getAccountId(),
                        updatedBy,
                        String.join(",", changedFields)
                );
                eventPublisher.publishAll(List.of(event));
                log.debug("Published ProfileUpdated event - accountId: {}, fields: {}, correlationId: {}", 
                        saved.getAccountId(), changedFields, correlationId);
            }
            
            log.info("Profile updated successfully - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
            return saved;
            
        } catch (Exception e) {
            log.error("Failed to update profile - accountId: {}, correlationId: {}", 
                    command.getAccountId(), correlationId, e);
            unitOfWork.rollback(e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public PrivacySettings updatePrivacySettings(UpdatePrivacySettingsCommand command, String updatedBy) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Updating privacy settings - accountId: {}, updatedBy: {}, correlationId: {}", 
                command.getAccountId(), updatedBy, correlationId);
        
        unitOfWork.begin();
        try {
            AccountProfile profile = profileRepository.findByAccountId(command.getAccountId())
                    .orElseThrow(() -> {
                        log.warn("Profile not found - accountId: {}, correlationId: {}", 
                                command.getAccountId(), correlationId);
                        return new IllegalArgumentException("Profile not found: " + command.getAccountId());
                    });
            
            Instant now = clock.now();
            PrivacySettings updatedSettings = PrivacySettings.builder()
                    .subscriptionsPrivacy(command.getSubscriptionsPrivacy())
                    .savedPlaylistsPrivacy(command.getSavedPlaylistsPrivacy())
                    .restrictedMode(command.getRestrictedMode())
                    .watchHistoryPrivacy(command.getWatchHistoryPrivacy())
                    .likeHistoryPrivacy(command.getLikeHistoryPrivacy())
                    .build();
            
            AccountProfile updated = profile.withPrivacySettings(updatedSettings, now, updatedBy, 
                    generateEtag(profile.getVersion() + 1, now));
            
            AccountProfile saved = profileRepository.update(updated);
            
            // Publish domain event
            PrivacySettingsChanged event = new PrivacySettingsChanged(
                    saved.getAccountId(),
                    updatedBy
            );
            eventPublisher.publishAll(List.of(event));
            log.debug("Published PrivacySettingsChanged event - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
            log.info("Privacy settings updated successfully - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
            return saved.getPrivacySettings();
            
        } catch (Exception e) {
            log.error("Failed to update privacy settings - accountId: {}, correlationId: {}", 
                    command.getAccountId(), correlationId, e);
            unitOfWork.rollback(e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public NotificationSettings updateNotificationSettings(UpdateNotificationSettingsCommand command, String updatedBy) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Updating notification settings - accountId: {}, updatedBy: {}, correlationId: {}", 
                command.getAccountId(), updatedBy, correlationId);
        
        unitOfWork.begin();
        try {
            AccountProfile profile = profileRepository.findByAccountId(command.getAccountId())
                    .orElseThrow(() -> {
                        log.warn("Profile not found - accountId: {}, correlationId: {}", 
                                command.getAccountId(), correlationId);
                        return new IllegalArgumentException("Profile not found: " + command.getAccountId());
                    });
            
            Instant now = clock.now();
            NotificationSettings updatedSettings = NotificationSettings.builder()
                    .emailOptIn(command.getEmailOptIn())
                    .pushNotifications(command.getPushNotifications())
                    .marketingCommunications(command.getMarketingCommunications())
                    .build();
            
            AccountProfile updated = profile.withNotificationSettings(updatedSettings, now, updatedBy, 
                    generateEtag(profile.getVersion() + 1, now));
            
            AccountProfile saved = profileRepository.update(updated);
            
            // Publish domain event
            NotificationPrefsChanged event = new NotificationPrefsChanged(
                    saved.getAccountId(),
                    updatedBy
            );
            eventPublisher.publishAll(List.of(event));
            log.debug("Published NotificationPrefsChanged event - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
            log.info("Notification settings updated successfully - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
            return saved.getNotificationSettings();
            
        } catch (Exception e) {
            log.error("Failed to update notification settings - accountId: {}, correlationId: {}", 
                    command.getAccountId(), correlationId, e);
            unitOfWork.rollback(e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public AccessibilityPreferences updateAccessibilityPreferences(UpdateAccessibilityPreferencesCommand command, String updatedBy) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Updating accessibility preferences - accountId: {}, updatedBy: {}, correlationId: {}", 
                command.getAccountId(), updatedBy, correlationId);
        
        unitOfWork.begin();
        try {
            AccountProfile profile = profileRepository.findByAccountId(command.getAccountId())
                    .orElseThrow(() -> {
                        log.warn("Profile not found - accountId: {}, correlationId: {}", 
                                command.getAccountId(), correlationId);
                        return new IllegalArgumentException("Profile not found: " + command.getAccountId());
                    });
            
            Instant now = clock.now();
            AccessibilityPreferences updatedPrefs = AccessibilityPreferences.builder()
                    .captionFontSize(command.getCaptionFontSize())
                    .highContrast(command.getHighContrast())
                    .reducedMotion(command.getReducedMotion())
                    .build();
            
            AccountProfile updated = profile.withAccessibilityPreferences(updatedPrefs, now, updatedBy, 
                    generateEtag(profile.getVersion() + 1, now));
            
            AccountProfile saved = profileRepository.update(updated);
            
            // Publish domain event
            AccessibilityPrefsChanged event = new AccessibilityPrefsChanged(
                    saved.getAccountId(),
                    updatedBy
            );
            eventPublisher.publishAll(List.of(event));
            log.debug("Published AccessibilityPrefsChanged event - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
            log.info("Accessibility preferences updated successfully - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
            return saved.getAccessibilityPreferences();
            
        } catch (Exception e) {
            log.error("Failed to update accessibility preferences - accountId: {}, correlationId: {}", 
                    command.getAccountId(), correlationId, e);
            unitOfWork.rollback(e);
            throw e;
        }
    }
    
    @Override
    public AccountProfile getProfile(GetProfileQuery query) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.debug("Getting profile - accountId: {}, correlationId: {}", query.getAccountId(), correlationId);
        
        AccountProfile profile = profileRepository.findByAccountId(query.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Profile not found - accountId: {}, correlationId: {}", 
                            query.getAccountId(), correlationId);
                    return new IllegalArgumentException("Profile not found: " + query.getAccountId());
                });
        
        log.debug("Profile retrieved - accountId: {}, correlationId: {}", profile.getAccountId(), correlationId);
        return profile;
    }
    
    @Override
    public PrivacySettings getPrivacySettings(GetPrivacySettingsQuery query) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.debug("Getting privacy settings - accountId: {}, correlationId: {}", 
                query.getAccountId(), correlationId);
        
        AccountProfile profile = profileRepository.findByAccountId(query.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Profile not found - accountId: {}, correlationId: {}", 
                            query.getAccountId(), correlationId);
                    return new IllegalArgumentException("Profile not found: " + query.getAccountId());
                });
        
        return profile.getPrivacySettings();
    }
    
    @Override
    public NotificationSettings getNotificationSettings(GetNotificationSettingsQuery query) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.debug("Getting notification settings - accountId: {}, correlationId: {}", 
                query.getAccountId(), correlationId);
        
        AccountProfile profile = profileRepository.findByAccountId(query.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Profile not found - accountId: {}, correlationId: {}", 
                            query.getAccountId(), correlationId);
                    return new IllegalArgumentException("Profile not found: " + query.getAccountId());
                });
        
        return profile.getNotificationSettings();
    }
    
    @Override
    public AccessibilityPreferences getAccessibilityPreferences(GetAccessibilityPreferencesQuery query) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.debug("Getting accessibility preferences - accountId: {}, correlationId: {}", 
                query.getAccountId(), correlationId);
        
        AccountProfile profile = profileRepository.findByAccountId(query.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Profile not found - accountId: {}, correlationId: {}", 
                            query.getAccountId(), correlationId);
                    return new IllegalArgumentException("Profile not found: " + query.getAccountId());
                });
        
        return profile.getAccessibilityPreferences();
    }
    
    private String generateEtag(int version, Instant timestamp) {
        return String.format("%d-%d", version, timestamp.toEpochMilli());
    }
}

