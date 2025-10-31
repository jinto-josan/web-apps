package com.youtube.contentidservice.interfaces.rest;

import com.youtube.contentidservice.application.commands.CreateFingerprintCommand;
import com.youtube.contentidservice.application.dto.FingerprintResponse;
import com.youtube.contentidservice.application.services.FingerprintService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fingerprint")
@RequiredArgsConstructor
@Tag(name = "Fingerprint", description = "Fingerprint management API")
public class FingerprintController {
    private final FingerprintService fingerprintService;

    @PostMapping("/{videoId}")
    @Operation(summary = "Create fingerprint for a video", description = "Generates and stores a fingerprint for the specified video")
    @ApiResponse(responseCode = "201", description = "Fingerprint created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PreAuthorize("hasAuthority('SCOPE_content.write')")
    public ResponseEntity<FingerprintResponse> createFingerprint(
            @Parameter(description = "Video ID") @PathVariable UUID videoId,
            @RequestBody(required = false) CreateFingerprintCommand command) {
        
        if (command == null) {
            command = new CreateFingerprintCommand(videoId, null);
        } else {
            // Ensure videoId matches path
            command = new CreateFingerprintCommand(videoId, command.getBlobUri());
        }

        FingerprintResponse response = fingerprintService.createFingerprint(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fingerprintId}")
    @Operation(summary = "Get fingerprint by ID")
    @ApiResponse(responseCode = "200", description = "Fingerprint found")
    @ApiResponse(responseCode = "404", description = "Fingerprint not found")
    @PreAuthorize("hasAuthority('SCOPE_content.read')")
    public ResponseEntity<FingerprintResponse> getFingerprint(
            @Parameter(description = "Fingerprint ID") @PathVariable UUID fingerprintId) {
        
        FingerprintResponse response = fingerprintService.getFingerprint(fingerprintId);
        return ResponseEntity.ok(response);
    }
}

