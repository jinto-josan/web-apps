package com.youtube.userprofileservice.interfaces.rest;

import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.application.commands.*;
import com.youtube.userprofileservice.application.queries.*;
import com.youtube.userprofileservice.application.usecases.ProfileUseCase;
import com.youtube.userprofileservice.domain.entities.*;
import com.youtube.userprofileservice.domain.services.PhotoUploadService;
import com.youtube.userprofileservice.interfaces.rest.dto.PhotoUploadCompleteRequest;
import com.youtube.userprofileservice.interfaces.rest.dto.PhotoUploadRequest;
import com.youtube.userprofileservice.interfaces.rest.dto.PhotoUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * REST controller for profile management.
 * Follows RESTful principles with proper HTTP methods and status codes.
 * Includes correlation ID tracking and structured logging.
 */
@Slf4j
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Profiles", description = "API for managing user account profiles and preferences")
public class ProfileController {
    
    private final ProfileUseCase profileUseCase;
    private final PhotoUploadService photoUploadService;
    
    @GetMapping("/{accountId}")
    @Operation(summary = "Get user profile", description = "Retrieves a user's account profile")
    public ResponseEntity<AccountProfile> getProfile(
            @Parameter(description = "Account ID", required = true)
            @PathVariable @NotBlank String accountId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.info("GET /profiles/{} - userId: {}, correlationId: {}", accountId, userId, correlationId);
        
        try {
            GetProfileQuery query = GetProfileQuery.builder()
                    .accountId(accountId)
                    .build();
            
            AccountProfile profile = profileUseCase.getProfile(query);
            
            log.debug("Profile retrieved successfully - accountId: {}, correlationId: {}", 
                    accountId, correlationId);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.ETAG, profile.getEtag())
                    .body(profile);
        } catch (Exception e) {
            log.error("Failed to get profile - accountId: {}, userId: {}, correlationId: {}", 
                    accountId, userId, correlationId, e);
            throw e;
        }
    }
    
    @PatchMapping("/{accountId}")
    @Operation(summary = "Update user profile", description = "Updates a user's account profile with optimistic locking")
    public ResponseEntity<AccountProfile> updateProfile(
            @Parameter(description = "Account ID", required = true)
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid UpdateProfileCommand command,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.info("PATCH /profiles/{} - userId: {}, correlationId: {}, ifMatch: {}", 
                accountId, userId, correlationId, ifMatch);
        
        try {
            command.setAccountId(accountId);
            command.setEtag(ifMatch);
            
            AccountProfile updated = profileUseCase.updateProfile(command, userId);
            
            log.info("Profile updated successfully - accountId: {}, correlationId: {}", 
                    accountId, correlationId);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.ETAG, updated.getEtag())
                    .body(updated);
        } catch (Exception e) {
            log.error("Failed to update profile - accountId: {}, userId: {}, correlationId: {}", 
                    accountId, userId, correlationId, e);
            throw e;
        }
    }
    
    @GetMapping("/{accountId}/privacy")
    @Operation(summary = "Get privacy settings", description = "Retrieves a user's privacy settings")
    public ResponseEntity<PrivacySettings> getPrivacySettings(
            @PathVariable @NotBlank String accountId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.debug("GET /profiles/{}/privacy - userId: {}, correlationId: {}", accountId, userId, correlationId);
        
        GetPrivacySettingsQuery query = GetPrivacySettingsQuery.builder()
                .accountId(accountId)
                .build();
        
        PrivacySettings settings = profileUseCase.getPrivacySettings(query);
        
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping("/{accountId}/privacy")
    @Operation(summary = "Update privacy settings", description = "Updates a user's privacy settings")
    public ResponseEntity<PrivacySettings> updatePrivacySettings(
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid UpdatePrivacySettingsCommand command,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.info("PUT /profiles/{}/privacy - userId: {}, correlationId: {}", accountId, userId, correlationId);
        
        command.setAccountId(accountId);
        
        PrivacySettings updated = profileUseCase.updatePrivacySettings(command, userId);
        
        log.debug("Privacy settings updated successfully - accountId: {}, correlationId: {}", 
                accountId, correlationId);
        
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/{accountId}/notifications")
    @Operation(summary = "Get notification settings", description = "Retrieves a user's notification settings")
    public ResponseEntity<NotificationSettings> getNotificationSettings(
            @PathVariable @NotBlank String accountId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.debug("GET /profiles/{}/notifications - userId: {}, correlationId: {}", accountId, userId, correlationId);
        
        GetNotificationSettingsQuery query = GetNotificationSettingsQuery.builder()
                .accountId(accountId)
                .build();
        
        NotificationSettings settings = profileUseCase.getNotificationSettings(query);
        
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping("/{accountId}/notifications")
    @Operation(summary = "Update notification settings", description = "Updates a user's notification settings")
    public ResponseEntity<NotificationSettings> updateNotificationSettings(
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid UpdateNotificationSettingsCommand command,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.info("PUT /profiles/{}/notifications - userId: {}, correlationId: {}", accountId, userId, correlationId);
        
        command.setAccountId(accountId);
        
        NotificationSettings updated = profileUseCase.updateNotificationSettings(command, userId);
        
        log.debug("Notification settings updated successfully - accountId: {}, correlationId: {}", 
                accountId, correlationId);
        
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{accountId}/photo/upload-url")
    @Operation(summary = "Generate photo upload URL", 
               description = "Generates a pre-signed URL for uploading a profile photo. " +
                           "After upload, the photo will be automatically processed (virus scan and compression).")
    public ResponseEntity<PhotoUploadResponse> generatePhotoUploadUrl(
            @Parameter(description = "Account ID", required = true)
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid PhotoUploadRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.info("POST /profiles/{}/photo/upload-url - userId: {}, contentType: {}, maxSize: {} bytes, correlationId: {}",
                accountId, request.getContentType(), request.getMaxFileSizeBytes(), correlationId);
        
        try {
            PhotoUploadService.PhotoUploadUrl uploadUrl = photoUploadService.generateUploadUrl(
                    accountId,
                    request.getContentType(),
                    request.getMaxFileSizeBytes() != null ? request.getMaxFileSizeBytes() : 10485760L // Default 10MB
            );
            
            PhotoUploadResponse response = PhotoUploadResponse.builder()
                    .uploadUrl(uploadUrl.uploadUrl())
                    .blobName(uploadUrl.blobName())
                    .containerName(uploadUrl.containerName())
                    .expiresAt(uploadUrl.expiresAt())
                    .maxFileSizeBytes(uploadUrl.maxFileSizeBytes())
                    .durationMinutes(uploadUrl.durationMinutes())
                    .build();
            
            log.info("Photo upload URL generated successfully - accountId: {}, blobName: {}, correlationId: {}",
                    accountId, uploadUrl.blobName(), correlationId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate photo upload URL - accountId: {}, userId: {}, correlationId: {}",
                    accountId, userId, correlationId, e);
            throw e;
        }
    }
    
    @PostMapping("/{accountId}/photo/upload-complete")
    @Operation(summary = "Notify photo upload complete", 
               description = "Notifies the service that a photo upload is complete. " +
                           "This triggers virus scanning and compression processing via queue. " +
                           "Follows CQRS pattern - uses NotifyPhotoUploadCompleteCommand.")
    public ResponseEntity<Void> notifyPhotoUploadComplete(
            @Parameter(description = "Account ID", required = true)
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid PhotoUploadCompleteRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        String userId = jwt.getClaimAsString("sub");
        log.info("POST /profiles/{}/photo/upload-complete - userId: {}, blobName: {}, fileSize: {} bytes, correlationId: {}",
                accountId, request.getBlobName(), request.getFileSizeBytes(), correlationId);
        
        try {
            // Create command following CQRS pattern
            NotifyPhotoUploadCompleteCommand command = NotifyPhotoUploadCompleteCommand.builder()
                    .accountId(accountId)
                    .blobName(request.getBlobName())
                    .containerName("profile-photos") // Default container
                    .contentType(request.getContentType())
                    .fileSizeBytes(request.getFileSizeBytes())
                    .build();
            
            // Execute command via use case (CQRS pattern)
            profileUseCase.notifyPhotoUploadComplete(command);
            
            log.info("Photo upload completion notified - accountId: {}, blobName: {}, correlationId: {}",
                    accountId, request.getBlobName(), correlationId);
            
            // Return 202 Accepted for async processing
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Failed to notify photo upload completion - accountId: {}, userId: {}, correlationId: {}",
                    accountId, userId, correlationId, e);
            throw e;
        }
    }
}

