package com.youtube.mediaassist.interfaces.rest;

import com.youtube.mediaassist.application.usecases.MediaAccessUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for media access operations
 */
@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Media Access", description = "Media asset access and SAS issuance")
public class MediaController {
    
    private final MediaAccessUseCase mediaAccessUseCase;
    
    public MediaController(MediaAccessUseCase mediaAccessUseCase) {
        this.mediaAccessUseCase = mediaAccessUseCase;
    }
    
    @PostMapping("/sas")
    @Operation(summary = "Generate SAS URL", 
               description = "Generate a Shared Access Signature URL for secure blob access",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SasUrlResponse> generateSasUrl(
            @Valid @RequestBody SasUrlRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getClaimAsString("sub");
        
        MediaAccessUseCase.GenerateSignedUrlRequest useCaseRequest = 
                new MediaAccessUseCase.GenerateSignedUrlRequest();
        useCaseRequest.setUserId(userId);
        useCaseRequest.setPath(request.getPath());
        useCaseRequest.setContainer(request.getContainer());
        useCaseRequest.setValidityDuration(request.getValidityDuration());
        useCaseRequest.setPlayback(request.isPlayback());
        useCaseRequest.setIdempotencyKey(idempotencyKey);
        
        MediaAccessUseCase.GenerateSignedUrlResponse response = 
                mediaAccessUseCase.generateSignedUrl(useCaseRequest);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                .body(new SasUrlResponse(response.getUrl(), response.getExpiresAt()));
    }
    
    @GetMapping("/origin/{path:.+}")
    @Operation(summary = "Get signed origin URL", 
               description = "Get a signed URL for the origin blob route",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SasUrlResponse> getSignedOriginUrl(
            @PathVariable @NotBlank String path,
            @RequestParam(defaultValue = "renditions") String container,
            @RequestParam(defaultValue = "PT4H") String validity,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getClaimAsString("sub");
        
        SasUrlRequest request = new SasUrlRequest();
        request.setPath(path);
        request.setContainer(container);
        request.setValidityDuration(validity);
        request.setPlayback(true);
        
        MediaAccessUseCase.GenerateSignedUrlRequest useCaseRequest = 
                new MediaAccessUseCase.GenerateSignedUrlRequest();
        useCaseRequest.setUserId(userId);
        useCaseRequest.setPath(request.getPath());
        useCaseRequest.setContainer(request.getContainer());
        useCaseRequest.setValidityDuration(request.getValidityDuration());
        useCaseRequest.setPlayback(request.isPlayback());
        useCaseRequest.setIdempotencyKey(idempotencyKey);
        
        MediaAccessUseCase.GenerateSignedUrlResponse response = 
                mediaAccessUseCase.generateSignedUrl(useCaseRequest);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(new SasUrlResponse(response.getUrl(), response.getExpiresAt()));
    }
    
    @Data
    public static class SasUrlRequest {
        @NotBlank(message = "Path is required")
        @Pattern(regexp = "^[a-zA-Z0-9_\\-\\./]+$", message = "Invalid path format")
        private String path;
        
        @NotBlank(message = "Container is required")
        private String container;
        
        @Pattern(regexp = "^PT\\d+[HMS]$", message = "Invalid validity duration format")
        private String validityDuration = "PT1H";
        
        private boolean playback = false;
    }
    
    public static class SasUrlResponse {
        private final String url;
        private final Instant expiresAt;
        
        public SasUrlResponse(String url, Instant expiresAt) {
            this.url = url;
            this.expiresAt = expiresAt;
        }
        
        public String getUrl() { return url; }
        public Instant getExpiresAt() { return expiresAt; }
    }
}

