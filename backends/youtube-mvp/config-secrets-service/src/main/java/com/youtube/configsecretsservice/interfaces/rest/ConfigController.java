package com.youtube.configsecretsservice.interfaces.rest;

import com.youtube.configsecretsservice.application.dto.ConfigRequest;
import com.youtube.configsecretsservice.application.dto.ConfigResponse;
import com.youtube.configsecretsservice.application.service.ConfigurationApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for configuration operations.
 */
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configuration", description = "Configuration management API")
public class ConfigController {
    
    private final ConfigurationApplicationService applicationService;
    
    @GetMapping("/{scope}/{key}")
    @Operation(summary = "Get configuration", description = "Retrieve a configuration value by scope and key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ConfigResponse.class))),
            @ApiResponse(responseCode = "304", description = "Not modified (ETag match)"),
            @ApiResponse(responseCode = "404", description = "Configuration not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ConfigResponse> getConfiguration(
            @Parameter(description = "Configuration scope (tenant/environment)") @PathVariable String scope,
            @Parameter(description = "Configuration key") @PathVariable String key,
            @Parameter(description = "ETag for conditional request") @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        String tenantId = jwt.getClaimAsString("tid");
        
        try {
            ConfigResponse response = applicationService.getConfiguration(scope, key, userId, tenantId, ifNoneMatch);
            return ResponseEntity.ok()
                    .eTag(response.getEtag())
                    .body(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            throw e;
        }
    }
    
    @PutMapping("/{scope}/{key}")
    @Operation(summary = "Update configuration", description = "Create or update a configuration value")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully",
                    content = @Content(schema = @Schema(implementation = ConfigResponse.class))),
            @ApiResponse(responseCode = "412", description = "Precondition failed (ETag mismatch)"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ConfigResponse> updateConfiguration(
            @Parameter(description = "Configuration scope (tenant/environment)") @PathVariable String scope,
            @Parameter(description = "Configuration key") @PathVariable String key,
            @Valid @RequestBody ConfigRequest request,
            @Parameter(description = "ETag for optimistic concurrency") @RequestHeader(value = "If-Match", required = false) String ifMatch,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        String tenantId = jwt.getClaimAsString("tid");
        
        ConfigResponse response = applicationService.updateConfiguration(scope, key, request, ifMatch, userId, tenantId);
        return ResponseEntity.ok()
                .eTag(response.getEtag())
                .body(response);
    }
}

