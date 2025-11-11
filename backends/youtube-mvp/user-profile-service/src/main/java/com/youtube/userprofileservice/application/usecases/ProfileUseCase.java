package com.youtube.userprofileservice.application.usecases;

import com.youtube.userprofileservice.application.commands.*;
import com.youtube.userprofileservice.application.queries.*;
import com.youtube.userprofileservice.domain.entities.AccountProfile;
import com.youtube.userprofileservice.domain.entities.AccessibilityPreferences;
import com.youtube.userprofileservice.domain.entities.NotificationSettings;
import com.youtube.userprofileservice.domain.entities.PrivacySettings;

/**
 * Use case interface for profile operations.
 * Follows CQRS pattern with separate read and write operations.
 */
public interface ProfileUseCase {
    
    // Write operations
    AccountProfile updateProfile(UpdateProfileCommand command, String updatedBy);
    
    PrivacySettings updatePrivacySettings(UpdatePrivacySettingsCommand command, String updatedBy);
    
    NotificationSettings updateNotificationSettings(UpdateNotificationSettingsCommand command, String updatedBy);
    
    AccessibilityPreferences updateAccessibilityPreferences(UpdateAccessibilityPreferencesCommand command, String updatedBy);
    
    /**
     * Notifies that a photo upload is complete and triggers processing.
     * This is a command that initiates asynchronous processing (virus scan, compression).
     * 
     * @param command the notification command
     */
    void notifyPhotoUploadComplete(NotifyPhotoUploadCompleteCommand command);
    
    // Read operations
    AccountProfile getProfile(GetProfileQuery query);
    
    PrivacySettings getPrivacySettings(GetPrivacySettingsQuery query);
    
    NotificationSettings getNotificationSettings(GetNotificationSettingsQuery query);
    
    AccessibilityPreferences getAccessibilityPreferences(GetAccessibilityPreferencesQuery query);
}

