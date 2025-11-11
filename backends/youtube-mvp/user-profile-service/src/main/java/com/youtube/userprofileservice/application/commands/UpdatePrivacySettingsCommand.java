package com.youtube.userprofileservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to update privacy settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePrivacySettingsCommand {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
    
    private Boolean subscriptionsPrivate;
    
    private Boolean savedPlaylistsPrivate;
    
    private Boolean restrictedModeEnabled;
    
    private Boolean watchHistoryPrivate;
    
    private Boolean likeHistoryPrivate;
    
    private String etag; // For optimistic locking
}

