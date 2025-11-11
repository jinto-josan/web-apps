package com.youtube.userprofileservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable JPA entity for notification settings.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsEmbeddable {
    
    @Column(name = "notification_email_opt_in")
    private Boolean emailOptIn;
    
    @Column(name = "notification_push_opt_in")
    private Boolean pushOptIn;
    
    @Column(name = "notification_marketing_opt_in")
    private Boolean marketingOptIn;
    
    @Column(name = "notification_channel_preferences", columnDefinition = "jsonb")
    private String channelPreferences; // JSON
    
    @Column(name = "notification_email_preferences", columnDefinition = "jsonb")
    private String emailPreferences; // JSON
    
    @Column(name = "notification_push_preferences", columnDefinition = "jsonb")
    private String pushPreferences; // JSON
}

