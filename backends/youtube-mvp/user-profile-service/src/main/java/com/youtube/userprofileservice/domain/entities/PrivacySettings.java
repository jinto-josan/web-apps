package com.youtube.userprofileservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Value object representing user privacy settings.
 * Controls visibility of account-owned artifacts like subscriptions and playlists.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettings {
    
    @Builder.Default
    @NotNull(message = "Subscriptions private flag cannot be null")
    private Boolean subscriptionsPrivate = false; // Hide subscriptions from public
    
    @Builder.Default
    @NotNull(message = "Saved playlists private flag cannot be null")
    private Boolean savedPlaylistsPrivate = false; // Hide saved playlists from public
    
    @Builder.Default
    @NotNull(message = "Restricted mode flag cannot be null")
    private Boolean restrictedModeEnabled = false; // YouTube restricted mode
    
    @Builder.Default
    @NotNull(message = "Watch history flag cannot be null")
    private Boolean watchHistoryPrivate = false;
    
    @Builder.Default
    @NotNull(message = "Like history flag cannot be null")
    private Boolean likeHistoryPrivate = false;
}

