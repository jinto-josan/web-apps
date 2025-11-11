package com.youtube.userprofileservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable JPA entity for privacy settings.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsEmbeddable {
    
    @Column(name = "privacy_subscriptions_private")
    private Boolean subscriptionsPrivate;
    
    @Column(name = "privacy_saved_playlists_private")
    private Boolean savedPlaylistsPrivate;
    
    @Column(name = "privacy_restricted_mode_enabled")
    private Boolean restrictedModeEnabled;
    
    @Column(name = "privacy_watch_history_private")
    private Boolean watchHistoryPrivate;
    
    @Column(name = "privacy_like_history_private")
    private Boolean likeHistoryPrivate;
}

