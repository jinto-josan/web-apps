package com.youtube.userprofileservice.domain.services;

import com.youtube.userprofileservice.domain.events.AccessibilityPrefsChanged;
import com.youtube.userprofileservice.domain.events.NotificationPrefsChanged;
import com.youtube.userprofileservice.domain.events.PrivacySettingsChanged;
import com.youtube.userprofileservice.domain.events.ProfileUpdated;

/**
 * Domain service interface for publishing events.
 * Abstracts event publishing to maintain clean architecture.
 */
public interface EventPublisher {
    
    /**
     * Publishes a profile updated event.
     */
    void publishProfileUpdated(ProfileUpdated event);
    
    /**
     * Publishes a privacy settings changed event.
     */
    void publishPrivacySettingsChanged(PrivacySettingsChanged event);
    
    /**
     * Publishes a notification preferences changed event.
     */
    void publishNotificationPrefsChanged(NotificationPrefsChanged event);
    
    /**
     * Publishes an accessibility preferences changed event.
     */
    void publishAccessibilityPrefsChanged(AccessibilityPrefsChanged event);
}

