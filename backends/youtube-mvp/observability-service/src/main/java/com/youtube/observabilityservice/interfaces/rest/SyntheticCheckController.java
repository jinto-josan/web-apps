package com.youtube.observabilityservice.interfaces.rest;

import com.youtube.observabilityservice.application.dto.*;
import com.youtube.observabilityservice.application.service.SyntheticCheckApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/synthetics")
@RequiredArgsConstructor
@Tag(name = "Synthetic Checks", description = "Synthetic monitoring checks management")
public class SyntheticCheckController {
    
    private final SyntheticCheckApplicationService checkApplicationService;
    
    @PostMapping
    @Operation(summary = "Create a new synthetic check")
    public ResponseEntity<SyntheticCheckResponse> createCheck(
            @Valid @RequestBody CreateSyntheticCheckRequest request) {
        SyntheticCheckResponse response = checkApplicationService.createCheck(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{checkId}")
    @Operation(summary = "Get synthetic check by ID")
    public ResponseEntity<SyntheticCheckResponse> getCheck(@PathVariable String checkId) {
        SyntheticCheckResponse response = checkApplicationService.getCheck(checkId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all synthetic checks")
    public ResponseEntity<List<SyntheticCheckResponse>> getAllChecks() {
        List<SyntheticCheckResponse> responses = checkApplicationService.getAllChecks();
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{checkId}/run")
    @Operation(summary = "Manually trigger a synthetic check")
    public ResponseEntity<SyntheticCheckResultResponse> runCheck(@PathVariable String checkId) {
        SyntheticCheckResultResponse response = checkApplicationService.runCheck(checkId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{checkId}/enable")
    @Operation(summary = "Enable a synthetic check")
    public ResponseEntity<Void> enableCheck(@PathVariable String checkId) {
        checkApplicationService.enableCheck(checkId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{checkId}/disable")
    @Operation(summary = "Disable a synthetic check")
    public ResponseEntity<Void> disableCheck(@PathVariable String checkId) {
        checkApplicationService.disableCheck(checkId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{checkId}")
    @Operation(summary = "Delete a synthetic check")
    public ResponseEntity<Void> deleteCheck(@PathVariable String checkId) {
        checkApplicationService.deleteCheck(checkId);
        return ResponseEntity.noContent().build();
    }
}

