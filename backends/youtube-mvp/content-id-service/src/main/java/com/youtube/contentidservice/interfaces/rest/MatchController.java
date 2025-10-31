package com.youtube.contentidservice.interfaces.rest;

import com.youtube.contentidservice.application.commands.CreateMatchCommand;
import com.youtube.contentidservice.application.dto.MatchRequest;
import com.youtube.contentidservice.application.dto.MatchResponse;
import com.youtube.contentidservice.application.services.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
@Tag(name = "Match", description = "Fingerprint matching API")
public class MatchController {
    private final MatchService matchService;

    @PostMapping
    @Operation(summary = "Find matches for a fingerprint", description = "Searches for similar fingerprints above the threshold")
    @ApiResponse(responseCode = "200", description = "Matches found")
    @PreAuthorize("hasAuthority('SCOPE_content.read')")
    public ResponseEntity<List<MatchResponse>> findMatches(@Valid @RequestBody MatchRequest request) {
        List<MatchResponse> matches = matchService.findMatches(request);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/create")
    @Operation(summary = "Create a match record")
    @ApiResponse(responseCode = "201", description = "Match created")
    @PreAuthorize("hasAuthority('SCOPE_content.write')")
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody CreateMatchCommand command) {
        MatchResponse response = matchService.createMatch(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

