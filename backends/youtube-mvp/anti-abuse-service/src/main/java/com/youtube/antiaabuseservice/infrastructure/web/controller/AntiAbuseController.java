package com.youtube.antiaabuseservice.infrastructure.web.controller;

import com.youtube.antiaabuseservice.application.dto.RiskScoreRequest;
import com.youtube.antiaabuseservice.application.dto.RiskScoreResponse;
import com.youtube.antiaabuseservice.application.dto.RuleEvaluationRequest;
import com.youtube.antiaabuseservice.application.dto.RuleEvaluationResponse;
import com.youtube.antiaabuseservice.application.service.RiskScoringService;
import com.youtube.antiaabuseservice.application.service.RuleEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Anti-Abuse", description = "Risk scoring and fraud detection API")
public class AntiAbuseController {
    private final RiskScoringService riskScoringService;
    private final RuleEvaluationService ruleEvaluationService;

    @PostMapping("/risk/score")
    @Operation(summary = "Calculate risk score", description = "Returns risk score for an event using ML and rules")
    @ApiResponse(responseCode = "200", description = "Successfully calculated risk score")
    public ResponseEntity<RiskScoreResponse> calculateRiskScore(
            @Valid @RequestBody RiskScoreRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        RiskScoreResponse response = riskScoringService.calculateRiskScore(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rules/evaluate")
    @Operation(summary = "Evaluate rules", description = "Evaluates rules against provided features")
    @ApiResponse(responseCode = "200", description = "Successfully evaluated rules")
    public ResponseEntity<RuleEvaluationResponse> evaluateRules(
            @Valid @RequestBody RuleEvaluationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        RuleEvaluationResponse response = ruleEvaluationService.evaluateRules(request);
        return ResponseEntity.ok(response);
    }
}

