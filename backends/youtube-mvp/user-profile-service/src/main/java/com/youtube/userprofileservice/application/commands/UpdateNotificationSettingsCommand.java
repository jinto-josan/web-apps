package com.youtube.userprofileservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Command to update notification settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationSettingsCommand {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
    
    private Boolean emailOptIn;
    
    private Boolean pushOptIn;
    
    private Boolean marketingOptIn;
    
    private Map<String, Boolean> channelPreferences;
    
    private Map<String, Boolean> emailPreferences;
    
    private Map<String, Boolean> pushPreferences;
    
    private String etag; // For optimistic locking
}

