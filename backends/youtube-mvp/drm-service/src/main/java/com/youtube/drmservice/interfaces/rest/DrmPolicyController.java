package com.youtube.drmservice.interfaces.rest;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.drmservice.application.commands.CreateDrmPolicyCommand;
import com.youtube.drmservice.application.commands.RotateKeysCommand;
import com.youtube.drmservice.application.commands.UpdateDrmPolicyCommand;
import com.youtube.drmservice.application.queries.GetDrmPolicyByVideoIdQuery;
import com.youtube.drmservice.application.queries.GetDrmPolicyQuery;
import com.youtube.drmservice.application.usecases.DrmPolicyUseCase;
import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.models.KeyRotationPolicy;
import com.youtube.drmservice.domain.models.PolicyConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drm/policies")
@Tag(name = "DRM Policy", description = "DRM policy management APIs")
public class DrmPolicyController {

    private final DrmPolicyUseCase drmPolicyUseCase;

    public DrmPolicyController(DrmPolicyUseCase drmPolicyUseCase) {
        this.drmPolicyUseCase = drmPolicyUseCase;
    }

    @PostMapping
    @Operation(summary = "Create DRM policy", description = "Create a new DRM policy for a video")
    public ResponseEntity<DrmPolicyResponse> createPolicy(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateDrmPolicyRequest request) {
        
        String userId = JwtUser.userId(jwt);
        
        PolicyConfiguration config = PolicyConfiguration.builder()
                .contentKeyPolicyName(request.getContentKeyPolicyName())
                .licenseConfiguration(request.getLicenseConfiguration())
                .allowedApplications(request.getAllowedApplications())
                .persistentLicenseAllowed(request.getPersistentLicenseAllowed())
                .analogVideoProtection(request.getAnalogVideoProtection())
                .analogAudioProtection(request.getAnalogAudioProtection())
                .uncompressedVideoProtection(request.getUncompressedVideoProtection())
                .uncompressedAudioProtection(request.getUncompressedAudioProtection())
                .allowPassingVideoContentToUnknownOutput(request.getAllowPassingVideoContentToUnknownOutput())
                .allowPassingAudioContentToUnknownOutput(request.getAllowPassingAudioContentToUnknownOutput())
                .allowedTrackTypes(request.getAllowedTrackTypes())
                .playRightConfig(request.getPlayRightConfig())
                .licenseType(request.getLicenseType())
                .allowIdle(request.getAllowIdle())
                .rentalDurationSeconds(request.getRentalDurationSeconds())
                .rentalPlaybackDurationSeconds(request.getRentalPlaybackDurationSeconds())
                .build();
        
        KeyRotationPolicy rotationPolicy = null;
        if (request.getRotationPolicy() != null) {
            rotationPolicy = KeyRotationPolicy.builder()
                    .enabled(request.getRotationPolicy().isEnabled())
                    .rotationInterval(Duration.parse(request.getRotationPolicy().getRotationInterval()))
                    .rotationKeyVaultUri(request.getRotationPolicy().getRotationKeyVaultUri())
                    .build();
        }
        
        CreateDrmPolicyCommand command = CreateDrmPolicyCommand.builder()
                .videoId(request.getVideoId())
                .provider(DrmPolicy.DrmProvider.valueOf(request.getProvider().toUpperCase()))
                .configuration(config)
                .rotationPolicy(rotationPolicy)
                .createdBy(userId)
                .idempotencyKey(idempotencyKey)
                .build();
        
        DrmPolicy policy = drmPolicyUseCase.createPolicy(command);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .eTag(policy.getVersion().toString())
                .body(mapToResponse(policy));
    }

    @GetMapping("/{policyId}")
    @Operation(summary = "Get DRM policy", description = "Get a DRM policy by ID")
    public ResponseEntity<DrmPolicyResponse> getPolicy(
            @PathVariable String policyId) {
        
        GetDrmPolicyQuery query = GetDrmPolicyQuery.builder()
                .policyId(policyId)
                .build();
        
        DrmPolicy policy = drmPolicyUseCase.getPolicy(query);
        
        return ResponseEntity.ok()
                .eTag(policy.getVersion().toString())
                .body(mapToResponse(policy));
    }

    @GetMapping("/video/{videoId}")
    @Operation(summary = "Get DRM policy by video ID", description = "Get a DRM policy for a video")
    public ResponseEntity<DrmPolicyResponse> getPolicyByVideoId(
            @PathVariable String videoId) {
        
        GetDrmPolicyByVideoIdQuery query = GetDrmPolicyByVideoIdQuery.builder()
                .videoId(videoId)
                .build();
        
        DrmPolicy policy = drmPolicyUseCase.getPolicyByVideoId(query);
        
        return ResponseEntity.ok()
                .eTag(policy.getVersion().toString())
                .body(mapToResponse(policy));
    }

    @PutMapping("/{policyId}")
    @Operation(summary = "Update DRM policy", description = "Update an existing DRM policy")
    public ResponseEntity<DrmPolicyResponse> updatePolicy(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String policyId,
            @RequestHeader("If-Match") String etag,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody UpdateDrmPolicyRequest request) {
        
        String userId = JwtUser.userId(jwt);
        
        PolicyConfiguration config = PolicyConfiguration.builder()
                .contentKeyPolicyName(request.getContentKeyPolicyName())
                .licenseConfiguration(request.getLicenseConfiguration())
                .allowedApplications(request.getAllowedApplications())
                .persistentLicenseAllowed(request.getPersistentLicenseAllowed())
                .analogVideoProtection(request.getAnalogVideoProtection())
                .analogAudioProtection(request.getAnalogAudioProtection())
                .uncompressedVideoProtection(request.getUncompressedVideoProtection())
                .uncompressedAudioProtection(request.getUncompressedAudioProtection())
                .allowPassingVideoContentToUnknownOutput(request.getAllowPassingVideoContentToUnknownOutput())
                .allowPassingAudioContentToUnknownOutput(request.getAllowPassingAudioContentToUnknownOutput())
                .allowedTrackTypes(request.getAllowedTrackTypes())
                .playRightConfig(request.getPlayRightConfig())
                .licenseType(request.getLicenseType())
                .allowIdle(request.getAllowIdle())
                .rentalDurationSeconds(request.getRentalDurationSeconds())
                .rentalPlaybackDurationSeconds(request.getRentalPlaybackDurationSeconds())
                .build();
        
        UpdateDrmPolicyCommand command = UpdateDrmPolicyCommand.builder()
                .policyId(policyId)
                .configuration(config)
                .updatedBy(userId)
                .etag(etag)
                .idempotencyKey(idempotencyKey)
                .build();
        
        DrmPolicy policy = drmPolicyUseCase.updatePolicy(command);
        
        return ResponseEntity.ok()
                .eTag(policy.getVersion().toString())
                .body(mapToResponse(policy));
    }

    @PostMapping("/rotate-keys")
    @Operation(summary = "Rotate keys", description = "Manually trigger key rotation for specified policies")
    public ResponseEntity<Void> rotateKeys(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RotateKeysRequest request) {
        
        String userId = JwtUser.userId(jwt);
        
        RotateKeysCommand command = RotateKeysCommand.builder()
                .policyIds(request.getPolicyIds())
                .rotatedBy(userId)
                .build();
        
        drmPolicyUseCase.rotateKeys(command);
        
        return ResponseEntity.noContent().build();
    }

    private DrmPolicyResponse mapToResponse(DrmPolicy policy) {
        RotationPolicyResponse rotationPolicyResponse = null;
        if (policy.getRotationPolicy() != null) {
            rotationPolicyResponse = RotationPolicyResponse.builder()
                    .enabled(policy.getRotationPolicy().isEnabled())
                    .rotationInterval(policy.getRotationPolicy().getRotationInterval().toString())
                    .lastRotationAt(policy.getRotationPolicy().getLastRotationAt())
                    .nextRotationAt(policy.getRotationPolicy().getNextRotationAt())
                    .rotationKeyVaultUri(policy.getRotationPolicy().getRotationKeyVaultUri())
                    .build();
        }
        
        return DrmPolicyResponse.builder()
                .id(policy.getId())
                .videoId(policy.getVideoId())
                .provider(policy.getProvider().name())
                .configuration(mapConfiguration(policy.getConfiguration()))
                .rotationPolicy(rotationPolicyResponse)
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .createdBy(policy.getCreatedBy())
                .updatedBy(policy.getUpdatedBy())
                .version(policy.getVersion())
                .build();
    }

    private PolicyConfigurationResponse mapConfiguration(PolicyConfiguration config) {
        return PolicyConfigurationResponse.builder()
                .contentKeyPolicyName(config.getContentKeyPolicyName())
                .licenseConfiguration(config.getLicenseConfiguration())
                .allowedApplications(config.getAllowedApplications())
                .persistentLicenseAllowed(config.getPersistentLicenseAllowed())
                .analogVideoProtection(config.getAnalogVideoProtection())
                .analogAudioProtection(config.getAnalogAudioProtection())
                .uncompressedVideoProtection(config.getUncompressedVideoProtection())
                .uncompressedAudioProtection(config.getUncompressedAudioProtection())
                .allowPassingVideoContentToUnknownOutput(config.getAllowPassingVideoContentToUnknownOutput())
                .allowPassingAudioContentToUnknownOutput(config.getAllowPassingAudioContentToUnknownOutput())
                .allowedTrackTypes(config.getAllowedTrackTypes())
                .playRightConfig(config.getPlayRightConfig())
                .licenseType(config.getLicenseType())
                .allowIdle(config.getAllowIdle())
                .rentalDurationSeconds(config.getRentalDurationSeconds())
                .rentalPlaybackDurationSeconds(config.getRentalPlaybackDurationSeconds())
                .build();
    }

    // Helper class to extract user ID from JWT
    static class JwtUser {
        static String userId(Jwt jwt) {
            String uid = jwt.getClaimAsString("uid");
            return (uid != null && !uid.isBlank()) ? uid : jwt.getSubject();
        }
    }

    // DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDrmPolicyRequest {
        private String videoId;
        private String provider; // WIDEVINE, PLAYREADY, FAIRPLAY
        private String contentKeyPolicyName;
        private Map<String, String> licenseConfiguration;
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
        private RotationPolicyDto rotationPolicy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RotationPolicyDto {
        private Boolean enabled;
        private String rotationInterval; // ISO-8601 duration
        private String rotationKeyVaultUri;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDrmPolicyRequest {
        private String contentKeyPolicyName;
        private Map<String, String> licenseConfiguration;
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

    @Data
    @Builder
    public static class DrmPolicyResponse {
        private String id;
        private String videoId;
        private String provider;
        private PolicyConfigurationResponse configuration;
        private RotationPolicyResponse rotationPolicy;
        private java.time.Instant createdAt;
        private java.time.Instant updatedAt;
        private String createdBy;
        private String updatedBy;
        private Long version;
    }

    @Data
    @Builder
    public static class PolicyConfigurationResponse {
        private String contentKeyPolicyName;
        private Map<String, String> licenseConfiguration;
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

    @Data
    @Builder
    public static class RotationPolicyResponse {
        private Boolean enabled;
        private String rotationInterval;
        private java.time.Instant lastRotationAt;
        private java.time.Instant nextRotationAt;
        private String rotationKeyVaultUri;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RotateKeysRequest {
        private List<String> policyIds;
    }
}

