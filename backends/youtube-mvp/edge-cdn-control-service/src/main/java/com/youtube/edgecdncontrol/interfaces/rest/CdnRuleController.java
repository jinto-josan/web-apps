package com.youtube.edgecdncontrol.interfaces.rest;

import com.youtube.edgecdncontrol.application.dto.*;
import com.youtube.edgecdncontrol.application.usecases.*;
import com.youtube.edgecdncontrol.domain.services.DriftDetectionService;
import com.youtube.edgecdncontrol.domain.valueobjects.CdnRuleId;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cdn/rules")
@Tag(name = "CDN Rules", description = "API for managing CDN/Front Door rules")
@RequiredArgsConstructor
public class CdnRuleController {
    
    private final CreateCdnRuleUseCase createCdnRuleUseCase;
    private final ApplyCdnRuleUseCase applyCdnRuleUseCase;
    private final GetCdnRulesUseCase getCdnRulesUseCase;
    private final DetectDriftUseCase detectDriftUseCase;
    
    @PostMapping
    @Operation(summary = "Create a new CDN rule", description = "Creates a new CDN rule in DRAFT status")
    @ApiResponse(responseCode = "201", description = "Rule created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<CdnRuleResponse> createRule(
            @Valid @RequestBody CreateCdnRuleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String createdBy = jwt != null ? jwt.getSubject() : "system";
        CdnRuleResponse response = createCdnRuleUseCase.execute(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .eTag(response.getVersion())
                .body(response);
    }
    
    @GetMapping("/{ruleId}")
    @Operation(summary = "Get a CDN rule by ID")
    @ApiResponse(responseCode = "200", description = "Rule found")
    @ApiResponse(responseCode = "404", description = "Rule not found")
    public ResponseEntity<CdnRuleResponse> getRule(
            @PathVariable String ruleId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        // TODO: Implement get by ID use case
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping
    @Operation(summary = "List CDN rules", description = "Lists CDN rules with pagination")
    public ResponseEntity<PageResponse<CdnRuleResponse>> listRules(
            @Parameter(description = "Resource group") @RequestParam(required = false) String resourceGroup,
            @Parameter(description = "Front Door profile name") @RequestParam(required = false) String profileName,
            @Parameter(description = "Filter by status") @RequestParam(required = false) RuleStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<CdnRuleResponse> response;
        if (resourceGroup != null && profileName != null) {
            response = getCdnRulesUseCase.executeByProfile(resourceGroup, profileName, page, size);
        } else if (status != null) {
            response = getCdnRulesUseCase.executeByStatus(status, page, size);
        } else {
            // Return empty for now - would need a list all use case
            response = PageResponse.<CdnRuleResponse>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{ruleId}/apply")
    @Operation(summary = "Apply a CDN rule", description = "Applies a validated rule to Azure Front Door")
    @ApiResponse(responseCode = "200", description = "Rule applied successfully")
    @ApiResponse(responseCode = "400", description = "Invalid rule or validation failed")
    public ResponseEntity<CdnRuleResponse> applyRule(
            @PathVariable String ruleId,
            @Parameter(description = "Dry-run mode") @RequestParam(defaultValue = "false") boolean dryRun) {
        CdnRuleResponse response = applyCdnRuleUseCase.execute(CdnRuleId.of(ruleId), dryRun);
        return ResponseEntity.ok()
                .eTag(response.getVersion())
                .body(response);
    }
    
    @PostMapping("/{ruleId}/detect-drift")
    @Operation(summary = "Detect configuration drift", description = "Compares expected rule with actual Azure configuration")
    @ApiResponse(responseCode = "200", description = "Drift detection completed")
    public ResponseEntity<List<DriftDetectionService.DriftFinding>> detectDrift(@PathVariable String ruleId) {
        List<DriftDetectionService.DriftFinding> findings = detectDriftUseCase.execute(CdnRuleId.of(ruleId));
        return ResponseEntity.ok(findings);
    }
}

