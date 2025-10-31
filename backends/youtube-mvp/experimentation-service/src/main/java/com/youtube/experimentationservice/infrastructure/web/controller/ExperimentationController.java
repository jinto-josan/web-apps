package com.youtube.experimentationservice.infrastructure.web.controller;

import com.youtube.experimentationservice.application.dto.ExperimentResponse;
import com.youtube.experimentationservice.application.dto.FeatureFlagResponse;
import com.youtube.experimentationservice.application.service.ExperimentService;
import com.youtube.experimentationservice.application.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Experimentation", description = "Feature flags and experiment management API")
public class ExperimentationController {
    private final FeatureFlagService featureFlagService;
    private final ExperimentService experimentService;

    @GetMapping("/flags")
    @Operation(summary = "Get feature flags for user", description = "Returns all feature flags with evaluation for the given user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved feature flags")
    public ResponseEntity<List<FeatureFlagResponse>> getFlags(
            @Parameter(description = "User ID") @RequestParam(required = false) String userId,
            @RequestParam Map<String, String> context,
            @AuthenticationPrincipal Jwt jwt) {
        String effectiveUserId = userId != null ? userId : jwt.getSubject();
        Map<String, String> enrichedContext = new HashMap<>(context);
        if (jwt != null) {
            enrichedContext.put("userId", jwt.getSubject());
            enrichedContext.put("email", jwt.getClaimAsString("email"));
        }
        
        List<FeatureFlagResponse> flags = featureFlagService.getAllFlags(effectiveUserId, enrichedContext);
        return ResponseEntity.ok(flags);
    }

    @GetMapping("/flags/{key}")
    @Operation(summary = "Get specific feature flag", description = "Returns a specific feature flag with evaluation for the given user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved feature flag")
    @ApiResponse(responseCode = "304", description = "Not modified (ETag)")
    public ResponseEntity<FeatureFlagResponse> getFlag(
            @PathVariable @NotBlank String key,
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestParam Map<String, String> context,
            @AuthenticationPrincipal Jwt jwt) {
        String effectiveUserId = userId != null ? userId : jwt.getSubject();
        Map<String, String> enrichedContext = new HashMap<>(context);
        if (jwt != null) {
            enrichedContext.put("userId", jwt.getSubject());
            enrichedContext.put("email", jwt.getClaimAsString("email"));
        }
        
        FeatureFlagResponse flag = featureFlagService.getFlag(key, effectiveUserId, enrichedContext);
        
        // Simple ETag based on key + userId (in production, use hash of flag state)
        String etag = "\"" + key + "-" + effectiveUserId + "\"";
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                    .eTag(etag)
                    .build();
        }
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .eTag(etag)
                .body(flag);
    }

    @GetMapping("/experiments/{key}")
    @Operation(summary = "Get experiment variant for user", description = "Returns the assigned experiment variant for the given user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved experiment")
    @ApiResponse(responseCode = "404", description = "Experiment not found or user not enrolled")
    public ResponseEntity<ExperimentResponse> getExperiment(
            @PathVariable @NotBlank String key,
            @RequestParam(required = false) String userId,
            @RequestParam Map<String, String> context,
            @AuthenticationPrincipal Jwt jwt) {
        String effectiveUserId = userId != null ? userId : jwt.getSubject();
        Map<String, String> enrichedContext = new HashMap<>(context);
        if (jwt != null) {
            enrichedContext.put("userId", jwt.getSubject());
            enrichedContext.put("email", jwt.getClaimAsString("email"));
        }
        
        ExperimentResponse experiment = experimentService.getExperiment(key, effectiveUserId, enrichedContext);
        return ResponseEntity.ok(experiment);
    }
}

