package com.youtube.edgecdncontrol.interfaces.rest;

import com.youtube.edgecdncontrol.application.dto.PurgeRequestDto;
import com.youtube.edgecdncontrol.application.dto.PurgeResponse;
import com.youtube.edgecdncontrol.application.usecases.PurgeCacheUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cdn/purge")
@Tag(name = "Cache Purge", description = "API for purging CDN cache")
@RequiredArgsConstructor
public class PurgeController {
    
    private final PurgeCacheUseCase purgeCacheUseCase;
    
    @PostMapping
    @Operation(summary = "Purge CDN cache", description = "Purges cache for specified content paths")
    @ApiResponse(responseCode = "202", description = "Purge request accepted")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<PurgeResponse> purgeCache(
            @Valid @RequestBody PurgeRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {
        String requestedBy = jwt != null ? jwt.getSubject() : "system";
        PurgeResponse response = purgeCacheUseCase.execute(request, requestedBy);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

