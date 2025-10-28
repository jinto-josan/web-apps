package com.youtube.recommendationsservice.interfaces.rest;

import com.youtube.recommendationsservice.application.dto.RecommendationRequest;
import com.youtube.recommendationsservice.application.dto.RecommendationResponse;
import com.youtube.recommendationsservice.application.usecases.GetHomeRecommendationsUseCase;
import com.youtube.recommendationsservice.application.usecases.GetNextUpRecommendationsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/recs")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Video recommendation API")
public class RecommendationController {
    
    private final GetHomeRecommendationsUseCase getHomeRecommendationsUseCase;
    private final GetNextUpRecommendationsUseCase getNextUpRecommendationsUseCase;
    
    @GetMapping("/home")
    @Operation(summary = "Get home page recommendations", 
               description = "Returns personalized video recommendations for the home page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recommendations"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<RecommendationResponse> getHomeRecommendations(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String device,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String abTestVariant,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Received home recommendations request for userId: {}", userId);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .userId(userId)
            .limit(limit)
            .device(device)
            .location(location)
            .language(language)
            .abTestVariant(abTestVariant)
            .build();
        
        RecommendationResponse response = getHomeRecommendationsUseCase.execute(request);
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
            .header("X-Request-ID", getRequestId(jwt))
            .body(response);
    }
    
    @GetMapping("/next")
    @Operation(summary = "Get next-up recommendations", 
               description = "Returns recommended videos to watch next based on current video")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recommendations"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<RecommendationResponse> getNextUpRecommendations(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Current video ID", required = true)
            @RequestParam String videoId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String device,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String abTestVariant,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Received next-up recommendations request for userId: {}, videoId: {}", userId, videoId);
        
        RecommendationRequest request = RecommendationRequest.builder()
            .userId(userId)
            .videoId(videoId)
            .limit(limit)
            .device(device)
            .location(location)
            .language(language)
            .abTestVariant(abTestVariant)
            .build();
        
        RecommendationResponse response = getNextUpRecommendationsUseCase.execute(request);
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)))
            .header("X-Request-ID", getRequestId(jwt))
            .body(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
    
    private String getRequestId(Jwt jwt) {
        if (jwt != null && jwt.getId() != null) {
            return jwt.getId();
        }
        return java.util.UUID.randomUUID().toString();
    }
}

