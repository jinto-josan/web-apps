package com.youtube.configsecretsservice.interfaces.rest;

import com.youtube.configsecretsservice.application.dto.SecretRotationRequest;
import com.youtube.configsecretsservice.application.dto.SecretRotationResponse;
import com.youtube.configsecretsservice.application.service.SecretRotationApplicationService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for secret rotation operations.
 */
@RestController
@RequestMapping("/api/v1/secrets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Secrets", description = "Secret rotation API")
public class SecretController {
    
    private final SecretRotationApplicationService rotationService;
    
    @PostMapping("/{scope}/{key}/rotate")
    @Operation(summary = "Rotate secret", description = "Initiate secret rotation for a given scope and key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Secret rotation initiated successfully",
                    content = @Content(schema = @Schema(implementation = SecretRotationResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Secret rotation failed")
    })
    public ResponseEntity<SecretRotationResponse> rotateSecret(
            @Parameter(description = "Secret scope (tenant/environment)") @PathVariable String scope,
            @Parameter(description = "Secret key") @PathVariable String key,
            @Valid @RequestBody SecretRotationRequest request,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();
        String tenantId = jwt.getClaimAsString("tid");
        
        SecretRotationResponse response = rotationService.rotateSecret(scope, key, request, userId, tenantId);
        return ResponseEntity.ok(response);
    }
}

