package com.youtube.userprofileservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Value object representing user notification preferences.
 * Controls email, push, and marketing communications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {
    
    @Builder.Default
    @NotNull(message = "Email opt-in flag cannot be null")
    private Boolean emailOptIn = true;
    
    @Builder.Default
    @NotNull(message = "Push opt-in flag cannot be null")
    private Boolean pushOptIn = true;
    
    @Builder.Default
    @NotNull(message = "Marketing opt-in flag cannot be null")
    private Boolean marketingOptIn = false;
    
    // Per-channel notification preferences
    // Key: Channel ID, Value: true = enabled, false = disabled
    private Map<String, Boolean> channelPreferences;
    
    // Email notification preferences by type
    private Map<String, Boolean> emailPreferences; // Key: "subscriptions", "comments", etc.
    
    // Push notification preferences by type
    private Map<String, Boolean> pushPreferences; // Key: "subscriptions", "comments", etc.
}

