package com.youtube.drmservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyConfigurationEmbeddable {
    private String contentKeyPolicyName;
    
    @ElementCollection
    @CollectionTable(name = "policy_license_config", joinColumns = @JoinColumn(name = "policy_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> licenseConfiguration;
    
    @ElementCollection
    @CollectionTable(name = "policy_allowed_apps", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "application")
    private List<String> allowedApplications;
    
    private Boolean persistentLicenseAllowed;
    private Boolean analogVideoProtection;
    private Boolean analogAudioProtection;
    private Boolean uncompressedVideoProtection;
    private Boolean uncompressedAudioProtection;
    private Boolean allowPassingVideoContentToUnknownOutput;
    private Boolean allowPassingAudioContentToUnknownOutput;
    private String allowedTrackTypes;
    private String playRightConfig;
    private String licenseType;
    private Boolean allowIdle;
    private Long rentalDurationSeconds;
    private Long rentalPlaybackDurationSeconds;
}

