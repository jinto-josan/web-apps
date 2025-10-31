package com.youtube.contentidservice.interfaces.rest;

import com.youtube.contentidservice.application.commands.CreateClaimCommand;
import com.youtube.contentidservice.application.commands.ResolveClaimCommand;
import com.youtube.contentidservice.application.dto.ClaimResponse;
import com.youtube.contentidservice.application.services.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@Tag(name = "Claim", description = "Content ID claims and disputes API")
public class ClaimController {
    private final ClaimService claimService;

    @PostMapping
    @Operation(summary = "Create a content ID claim", description = "Creates a claim based on detected matches")
    @ApiResponse(responseCode = "201", description = "Claim created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PreAuthorize("hasAuthority('SCOPE_content.write')")
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody CreateClaimCommand command) {
        ClaimResponse response = claimService.createClaim(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{claimId}")
    @Operation(summary = "Get claim by ID")
    @ApiResponse(responseCode = "200", description = "Claim found")
    @ApiResponse(responseCode = "404", description = "Claim not found")
    @PreAuthorize("hasAuthority('SCOPE_content.read')")
    public ResponseEntity<ClaimResponse> getClaim(
            @Parameter(description = "Claim ID") @PathVariable UUID claimId) {
        
        ClaimResponse response = claimService.getClaim(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/video/{videoId}")
    @Operation(summary = "Get claims for a video")
    @PreAuthorize("hasAuthority('SCOPE_content.read')")
    public ResponseEntity<List<ClaimResponse>> getClaimsByVideo(
            @Parameter(description = "Video ID") @PathVariable UUID videoId) {
        
        List<ClaimResponse> claims = claimService.getClaimsByVideo(videoId);
        return ResponseEntity.ok(claims);
    }

    @PostMapping("/{claimId}/resolve")
    @Operation(summary = "Resolve a claim", description = "Resolves a claim with a dispute status")
    @ApiResponse(responseCode = "200", description = "Claim resolved")
    @PreAuthorize("hasAuthority('SCOPE_content.admin')")
    public ResponseEntity<ClaimResponse> resolveClaim(
            @Parameter(description = "Claim ID") @PathVariable UUID claimId,
            @Valid @RequestBody ResolveClaimCommand command) {
        
        // Ensure claimId matches path
        ResolveClaimCommand resolvedCommand = new ResolveClaimCommand(claimId, command.getDisputeStatus(), command.getResolution());
        ClaimResponse response = claimService.resolveClaim(resolvedCommand);
        return ResponseEntity.ok(response);
    }
}

