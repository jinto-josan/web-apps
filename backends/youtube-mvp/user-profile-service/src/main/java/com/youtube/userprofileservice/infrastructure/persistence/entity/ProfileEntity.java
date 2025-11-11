package com.youtube.userprofileservice.infrastructure.persistence.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.userprofileservice.domain.entities.*;
import com.youtube.userprofileservice.domain.valueobjects.CaptionFontSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for account profiles.
 */
@Entity
@Table(name = "account_profiles", schema = "user_profile", indexes = {
    @Index(name = "idx_account_profiles_account_id", columnList = "account_id")
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ProfileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "account_id", unique = true, nullable = false, length = 26)
    private String accountId; // ULID
    
    @Size(max = 100)
    @Column(name = "display_name", length = 100)
    private String displayName;
    
    @Size(max = 500)
    @Column(name = "photo_url", length = 500)
    private String photoUrl;
    
    @Size(max = 10)
    @Column(name = "locale", length = 10)
    private String locale;
    
    @Size(max = 5)
    @Column(name = "country", length = 5)
    private String country;
    
    @Size(max = 100)
    @Column(name = "timezone", length = 100)
    private String timezone;
    
    @Embedded
    private PrivacySettingsEmbeddable privacySettings;
    
    @Embedded
    private NotificationSettingsEmbeddable notificationSettings;
    
    @Embedded
    private AccessibilityPreferencesEmbeddable accessibilityPreferences;
    
    @NotNull
    @Column(name = "version", nullable = false)
    private Integer version;
    
    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "updated_by", length = 26)
    private String updatedBy;
    
    @Column(name = "etag")
    private String etag;
    
    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (version == null) {
            version = 1;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
    
    /**
     * Converts JPA entity to domain entity.
     */
    public AccountProfile toDomain() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Convert PrivacySettings
        PrivacySettings domainPrivacySettings = null;
        if (privacySettings != null) {
            domainPrivacySettings = PrivacySettings.builder()
                    .subscriptionsPrivate(privacySettings.getSubscriptionsPrivate() != null ? 
                            privacySettings.getSubscriptionsPrivate() : false)
                    .savedPlaylistsPrivate(privacySettings.getSavedPlaylistsPrivate() != null ? 
                            privacySettings.getSavedPlaylistsPrivate() : false)
                    .restrictedModeEnabled(privacySettings.getRestrictedModeEnabled() != null ? 
                            privacySettings.getRestrictedModeEnabled() : false)
                    .watchHistoryPrivate(privacySettings.getWatchHistoryPrivate() != null ? 
                            privacySettings.getWatchHistoryPrivate() : false)
                    .likeHistoryPrivate(privacySettings.getLikeHistoryPrivate() != null ? 
                            privacySettings.getLikeHistoryPrivate() : false)
                    .build();
        }
        
        // Convert NotificationSettings
        NotificationSettings domainNotificationSettings = null;
        if (notificationSettings != null) {
            try {
                Map<String, Boolean> channelPrefs = null;
                if (notificationSettings.getChannelPreferences() != null && !notificationSettings.getChannelPreferences().isEmpty()) {
                    channelPrefs = objectMapper.readValue(
                            notificationSettings.getChannelPreferences(),
                            new TypeReference<Map<String, Boolean>>() {});
                }
                
                Map<String, Boolean> emailPrefs = null;
                if (notificationSettings.getEmailPreferences() != null && !notificationSettings.getEmailPreferences().isEmpty()) {
                    emailPrefs = objectMapper.readValue(
                            notificationSettings.getEmailPreferences(),
                            new TypeReference<Map<String, Boolean>>() {});
                }
                
                Map<String, Boolean> pushPrefs = null;
                if (notificationSettings.getPushPreferences() != null && !notificationSettings.getPushPreferences().isEmpty()) {
                    pushPrefs = objectMapper.readValue(
                            notificationSettings.getPushPreferences(),
                            new TypeReference<Map<String, Boolean>>() {});
                }
                
                domainNotificationSettings = NotificationSettings.builder()
                        .emailOptIn(notificationSettings.getEmailOptIn() != null ? 
                                notificationSettings.getEmailOptIn() : true)
                        .pushOptIn(notificationSettings.getPushOptIn() != null ? 
                                notificationSettings.getPushOptIn() : true)
                        .marketingOptIn(notificationSettings.getMarketingOptIn() != null ? 
                                notificationSettings.getMarketingOptIn() : false)
                        .channelPreferences(channelPrefs)
                        .emailPreferences(emailPrefs)
                        .pushPreferences(pushPrefs)
                        .build();
            } catch (Exception e) {
                log.error("Failed to deserialize notification settings JSON - accountId: {}", accountId, e);
                // Use defaults if deserialization fails
                domainNotificationSettings = NotificationSettings.builder().build();
            }
        }
        
        // Convert AccessibilityPreferences
        AccessibilityPreferences domainAccessibilityPreferences = null;
        if (accessibilityPreferences != null) {
            CaptionFontSize fontSize = CaptionFontSize.MEDIUM;
            if (accessibilityPreferences.getCaptionsFontSize() != null) {
                try {
                    fontSize = CaptionFontSize.valueOf(accessibilityPreferences.getCaptionsFontSize().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid caption font size - accountId: {}, fontSize: {}", 
                            accountId, accessibilityPreferences.getCaptionsFontSize());
                }
            }
            
            domainAccessibilityPreferences = AccessibilityPreferences.builder()
                    .captionsAlwaysOn(accessibilityPreferences.getCaptionsAlwaysOn() != null ? 
                            accessibilityPreferences.getCaptionsAlwaysOn() : false)
                    .captionsLanguage(accessibilityPreferences.getCaptionsLanguage())
                    .autoplayDefault(accessibilityPreferences.getAutoplayDefault() != null ? 
                            accessibilityPreferences.getAutoplayDefault() : false)
                    .autoplayOnHome(accessibilityPreferences.getAutoplayOnHome() != null ? 
                            accessibilityPreferences.getAutoplayOnHome() : false)
                    .captionsFontSize(fontSize)
                    .captionsBackgroundOpacity(accessibilityPreferences.getCaptionsBackgroundOpacity() != null ? 
                            accessibilityPreferences.getCaptionsBackgroundOpacity() : 100)
                    .build();
        }
        
        return AccountProfile.builder()
                .accountId(accountId)
                .displayName(displayName)
                .photoUrl(photoUrl)
                .locale(locale)
                .country(country)
                .timezone(timezone)
                .privacySettings(domainPrivacySettings)
                .notificationSettings(domainNotificationSettings)
                .accessibilityPreferences(domainAccessibilityPreferences)
                .version(version != null ? version : 1)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .etag(etag)
                .build();
    }
    
    /**
     * Creates JPA entity from domain entity.
     */
    public static ProfileEntity fromDomain(AccountProfile profile) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Convert PrivacySettings
        PrivacySettingsEmbeddable privacySettingsEmbeddable = null;
        if (profile.getPrivacySettings() != null) {
            privacySettingsEmbeddable = PrivacySettingsEmbeddable.builder()
                    .subscriptionsPrivate(profile.getPrivacySettings().getSubscriptionsPrivate())
                    .savedPlaylistsPrivate(profile.getPrivacySettings().getSavedPlaylistsPrivate())
                    .restrictedModeEnabled(profile.getPrivacySettings().getRestrictedModeEnabled())
                    .watchHistoryPrivate(profile.getPrivacySettings().getWatchHistoryPrivate())
                    .likeHistoryPrivate(profile.getPrivacySettings().getLikeHistoryPrivate())
                    .build();
        }
        
        // Convert NotificationSettings
        NotificationSettingsEmbeddable notificationSettingsEmbeddable = null;
        if (profile.getNotificationSettings() != null) {
            try {
                String channelPrefsJson = null;
                if (profile.getNotificationSettings().getChannelPreferences() != null) {
                    channelPrefsJson = objectMapper.writeValueAsString(
                            profile.getNotificationSettings().getChannelPreferences());
                }
                
                String emailPrefsJson = null;
                if (profile.getNotificationSettings().getEmailPreferences() != null) {
                    emailPrefsJson = objectMapper.writeValueAsString(
                            profile.getNotificationSettings().getEmailPreferences());
                }
                
                String pushPrefsJson = null;
                if (profile.getNotificationSettings().getPushPreferences() != null) {
                    pushPrefsJson = objectMapper.writeValueAsString(
                            profile.getNotificationSettings().getPushPreferences());
                }
                
                notificationSettingsEmbeddable = NotificationSettingsEmbeddable.builder()
                        .emailOptIn(profile.getNotificationSettings().getEmailOptIn())
                        .pushOptIn(profile.getNotificationSettings().getPushOptIn())
                        .marketingOptIn(profile.getNotificationSettings().getMarketingOptIn())
                        .channelPreferences(channelPrefsJson)
                        .emailPreferences(emailPrefsJson)
                        .pushPreferences(pushPrefsJson)
                        .build();
            } catch (Exception e) {
                log.error("Failed to serialize notification settings JSON - accountId: {}", profile.getAccountId(), e);
                throw new RuntimeException("Failed to serialize notification settings", e);
            }
        }
        
        // Convert AccessibilityPreferences
        AccessibilityPreferencesEmbeddable accessibilityPreferencesEmbeddable = null;
        if (profile.getAccessibilityPreferences() != null) {
            accessibilityPreferencesEmbeddable = AccessibilityPreferencesEmbeddable.builder()
                    .captionsAlwaysOn(profile.getAccessibilityPreferences().getCaptionsAlwaysOn())
                    .captionsLanguage(profile.getAccessibilityPreferences().getCaptionsLanguage())
                    .autoplayDefault(profile.getAccessibilityPreferences().getAutoplayDefault())
                    .autoplayOnHome(profile.getAccessibilityPreferences().getAutoplayOnHome())
                    .captionsFontSize(profile.getAccessibilityPreferences().getCaptionsFontSize() != null ? 
                            profile.getAccessibilityPreferences().getCaptionsFontSize().name() : null)
                    .captionsBackgroundOpacity(profile.getAccessibilityPreferences().getCaptionsBackgroundOpacity())
                    .build();
        }
        
        return ProfileEntity.builder()
                .accountId(profile.getAccountId())
                .displayName(profile.getDisplayName())
                .photoUrl(profile.getPhotoUrl())
                .locale(profile.getLocale())
                .country(profile.getCountry())
                .timezone(profile.getTimezone())
                .privacySettings(privacySettingsEmbeddable)
                .notificationSettings(notificationSettingsEmbeddable)
                .accessibilityPreferences(accessibilityPreferencesEmbeddable)
                .version(profile.getVersion())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .updatedBy(profile.getUpdatedBy())
                .etag(profile.getEtag())
                .build();
    }
}

