package com.youtube.userprofileservice.interfaces.rest;

import com.youtube.userprofileservice.application.commands.*;
import com.youtube.userprofileservice.application.queries.*;
import com.youtube.userprofileservice.application.usecases.ProfileUseCase;
import com.youtube.userprofileservice.domain.entities.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * REST controller for profile management.
 * Follows RESTful principles with proper HTTP methods and status codes.
 */
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Profiles", description = "API for managing user account profiles and preferences")
public class ProfileController {
    
    private final ProfileUseCase profileUseCase;
    
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAuthority('SCOPE_profile.read')")
    @Operation(summary = "Get user profile", description = "Retrieves a user's account profile")
    public ResponseEntity<AccountProfile> getProfile(
            @Parameter(description = "Account ID", required = true)
            @PathVariable @NotBlank String accountId,
            @AuthenticationPrincipal Jwt jwt) {
        
        GetProfileQuery query = GetProfileQuery.builder()
                .accountId(accountId)
                .build();
        
        AccountProfile profile = profileUseCase.getProfile(query);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, profile.getEtag())
                .body(profile);
    }
    
    @PatchMapping("/{accountId}")
    @PreAuthorize("hasAuthority('SCOPE_profile.write')")
    @Operation(summary = "Update user profile", description = "Updates a user's account profile with optimistic locking")
    public ResponseEntity<AccountProfile> updateProfile(
            @Parameter(description = "Account ID", required = true)
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid UpdateProfileCommand command,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @AuthenticationPrincipal Jwt jwt) {
        
        command.setAccountId(accountId);
        command.setEtag(ifMatch);
        
        String userId = jwt.getClaimAsString("sub");
        AccountProfile updated = profileUseCase.updateProfile(command, userId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, updated.getEtag())
                .body(updated);
    }
    
    @GetMapping("/{accountId}/privacy")
    @PreAuthorize("hasAuthority('SCOPE_profile.read')")
    @Operation(summary = "Get privacy settings", description = "Retrieves a user's privacy settings")
    public ResponseEntity<PrivacySettings> getPrivacySettings(
            @PathVariable @NotBlank String accountId,
            @AuthenticationPrincipal Jwt jwt) {
        
        GetPrivacySettingsQuery query = GetPrivacySettingsQuery.builder()
                .accountId(accountId)
                .build();
        
        PrivacySettings settings = profileUseCase.getPrivacySettings(query);
        
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping("/{accountId}/privacy")
    @PreAuthorize("hasAuthority('SCOPE_profile.write')")
    @Operation(summary = "Update privacy settings", description = "Updates a user's privacy settings")
    public ResponseEntity<PrivacySettings> updatePrivacySettings(
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid UpdatePrivacySettingsCommand command,
            @AuthenticationPrincipal Jwt jwt) {
        
        command.setAccountId(accountId);
        
        String userId = jwt.getClaimAsString("sub");
        PrivacySettings updated = profileUseCase.updatePrivacySettings(command, userId);
        
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/{accountId}/notifications")
    @PreAuthorize("hasAuthority('SCOPE_profile.read')")
    @Operation(summary = "Get notification settings", description = "Retrieves a user's notification settings")
    public ResponseEntity<NotificationSettings> getNotificationSettings(
            @PathVariable @NotBlank String accountId,
            @AuthenticationPrincipal Jwt jwt) {
        
        GetNotificationSettingsQuery query = GetNotificationSettingsQuery.builder()
                .accountId(accountId)
                .build();
        
        NotificationSettings settings = profileUseCase.getNotificationSettings(query);
        
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping("/{accountId}/notifications")
    @PreAuthorize("hasAuthority('SCOPE_profile.write')")
    @Operation(summary = "Update notification settings", description = "Updates a user's notification settings")
    public ResponseEntity<NotificationSettings> updateNotificationSettings(
            @PathVariable @NotBlank String accountId,
            @RequestBody @Valid UpdateNotificationSettingsCommand command,
            @AuthenticationPrincipal Jwt jwt) {
        
        command.setAccountId(accountId);
        
        String userId = jwt.getClaimAsString("sub");
        NotificationSettings updated = profileUseCase.updateNotificationSettings(command, userId);
        
        return ResponseEntity.ok(updated);
    }
}

