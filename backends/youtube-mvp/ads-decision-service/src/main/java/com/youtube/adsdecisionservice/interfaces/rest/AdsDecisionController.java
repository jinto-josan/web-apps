package com.youtube.adsdecisionservice.interfaces.rest;

import com.youtube.adsdecisionservice.application.services.DecisionService;
import com.youtube.adsdecisionservice.interfaces.rest.dto.AdDecisionRequest;
import com.youtube.adsdecisionservice.interfaces.rest.dto.AdDecisionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ads")
@Validated
@Tag(name = "Ads Decision", description = "Real-time ad decisioning")
public class AdsDecisionController {

    private final DecisionService decisionService;

    public AdsDecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Operation(summary = "Get ad decision", description = "Decide best ad for a user and video context")
    @PostMapping("/decision")
    public ResponseEntity<AdDecisionResponse> decide(@Valid @RequestBody AdDecisionRequest request) {
        AdDecisionResponse response = decisionService.decide(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}


