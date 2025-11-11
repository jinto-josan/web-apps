package com.youtube.userprofileservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Aggregate root representing a user's account profile.
 * Manages profile information, privacy settings, notification preferences, and accessibility settings.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AccountProfile {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId; // ULID
    
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;
    
    @Size(max = 500, message = "Photo URL cannot exceed 500 characters")
    private String photoUrl;
    
    @Size(max = 10, message = "Locale code cannot exceed 10 characters")
    private String locale; // e.g., "en-US"
    
    @Size(max = 5, message = "Country code cannot exceed 5 characters")
    private String country; // e.g., "US"
    
    @Size(max = 100, message = "Timezone cannot exceed 100 characters")
    private String timezone; // e.g., "America/New_York"
    
    private PrivacySettings privacySettings;
    
    private NotificationSettings notificationSettings;
    
    private AccessibilityPreferences accessibilityPreferences;
    
    @Builder.Default
    private int version = 1; // For optimistic locking/ETag
    
    @NotNull(message = "Created at timestamp cannot be null")
    private Instant createdAt;
    
    @NotNull(message = "Updated at timestamp cannot be null")
    private Instant updatedAt;
    
    private String updatedBy; // User ID who last updated
    
    private String etag; // For optimistic concurrency control
    
    /**
     * Updates the display name with version increment and timestamp.
     */
    public AccountProfile withDisplayName(String newDisplayName, Instant now, String updatedBy, String newEtag) {
        return this.toBuilder()
                .displayName(newDisplayName)
                .version(this.version + 1)
                .updatedAt(now)
                .updatedBy(updatedBy)
                .etag(newEtag)
                .build();
    }
    
    /**
     * Updates privacy settings.
     */
    public AccountProfile withPrivacySettings(PrivacySettings newPrivacySettings, Instant now, String updatedBy, String newEtag) {
        return this.toBuilder()
                .privacySettings(newPrivacySettings)
                .version(this.version + 1)
                .updatedAt(now)
                .updatedBy(updatedBy)
                .etag(newEtag)
                .build();
    }
    
    /**
     * Updates notification settings.
     */
    public AccountProfile withNotificationSettings(NotificationSettings newNotificationSettings, Instant now, String updatedBy, String newEtag) {
        return this.toBuilder()
                .notificationSettings(newNotificationSettings)
                .version(this.version + 1)
                .updatedAt(now)
                .updatedBy(updatedBy)
                .etag(newEtag)
                .build();
    }
    
    /**
     * Updates accessibility preferences.
     */
    public AccountProfile withAccessibilityPreferences(AccessibilityPreferences newAccessibilityPreferences, Instant now, String updatedBy, String newEtag) {
        return this.toBuilder()
                .accessibilityPreferences(newAccessibilityPreferences)
                .version(this.version + 1)
                .updatedAt(now)
                .updatedBy(updatedBy)
                .etag(newEtag)
                .build();
    }
}

