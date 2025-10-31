package com.youtube.observabilityservice.interfaces.rest;

import com.youtube.observabilityservice.application.dto.CreateSLORequest;
import com.youtube.observabilityservice.application.dto.SLOResponse;
import com.youtube.observabilityservice.application.service.SLOApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slo")
@RequiredArgsConstructor
@Tag(name = "SLO", description = "Service Level Objective management")
public class SLOController {
    
    private final SLOApplicationService sloApplicationService;
    
    @PostMapping
    @Operation(summary = "Create a new SLO")
    public ResponseEntity<SLOResponse> createSLO(@Valid @RequestBody CreateSLORequest request) {
        SLOResponse response = sloApplicationService.createSLO(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{sloId}")
    @Operation(summary = "Get SLO by ID")
    public ResponseEntity<SLOResponse> getSLO(@PathVariable String sloId) {
        SLOResponse response = sloApplicationService.getSLO(sloId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all SLOs")
    public ResponseEntity<List<SLOResponse>> getAllSLOs(
            @RequestParam(required = false) String serviceName) {
        List<SLOResponse> responses;
        if (serviceName != null) {
            responses = sloApplicationService.getSLOsByService(serviceName);
        } else {
            responses = sloApplicationService.getAllSLOs();
        }
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/{sloId}/recalculate")
    @Operation(summary = "Trigger SLO recalculation")
    public ResponseEntity<Void> recalculateSLO(@PathVariable String sloId) {
        sloApplicationService.recalculateSLO(sloId);
        return ResponseEntity.accepted().build();
    }
}

