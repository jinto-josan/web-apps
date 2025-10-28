package com.youtube.drmservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.List;
import java.util.Map;

/**
 * Value object for DRM policy configuration
 */
@Value
@Builder
@AllArgsConstructor
public class PolicyConfiguration {
    String contentKeyPolicyName;
    Map<String, String> licenseConfiguration;
    List<String> allowedApplications;
    Boolean persistentLicenseAllowed;
    Boolean analogVideoProtection;
    Boolean analogAudioProtection;
    Boolean uncompressedVideoProtection;
    Boolean uncompressedAudioProtection;
    
    // Widevine specific
    Boolean allowPassingVideoContentToUnknownOutput;
    Boolean allowPassingAudioContentToUnknownOutput;
    String allowedTrackTypes;
    
    // PlayReady specific
    String playRightConfig;
    String licenseType;
    
    // FairPlay specific
    Boolean allowIdle;
    Long rentalDurationSeconds;
    Long rentalPlaybackDurationSeconds;
}

