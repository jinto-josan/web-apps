package com.youtube.mvp.feeds.infrastructure.web;

import com.youtube.mvp.feeds.application.dto.FeedDto;
import com.youtube.mvp.feeds.application.usecase.GetFeedUseCase;
import com.youtube.mvp.feeds.domain.model.FeedType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
@Tag(name = "Feeds", description = "Feed management endpoints")
@SecurityRequirement(name = "OAuth2")
public class FeedController {
    
    private final GetFeedUseCase getFeedUseCase;
    
    @GetMapping("/home")
    @Operation(
            summary = "Get personalized home feed",
            description = "Returns a personalized home feed for the authenticated user with ad slots",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feed retrieved successfully",
                            content = @Content(schema = @Schema(implementation = FeedDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Feed not found")
            }
    )
    public ResponseEntity<FeedDto> getHomeFeed(
            @Parameter(description = "Number of items to return (max 100)", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer pageSize,
            
            @Parameter(description = "Page token for pagination")
            @RequestParam(required = false) String pageToken,
            
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            
            JwtAuthenticationToken authentication) {
        
        String userId = extractUserId(authentication);
        
        log.info("Getting home feed for userId={}, pageSize={}", userId, pageSize);
        
        FeedDto feed = getFeedUseCase.execute(userId, FeedType.HOME, pageSize, pageToken);
        
        // Check ETag for caching
        if (ifNoneMatch != null && ifNoneMatch.equals(feed.getEtag())) {
            return ResponseEntity.status(304)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                    .header(HttpHeaders.ETAG, feed.getEtag())
                    .build();
        }
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                .header(HttpHeaders.ETAG, feed.getEtag())
                .body(feed);
    }
    
    @GetMapping("/subscriptions")
    @Operation(
            summary = "Get subscriptions feed",
            description = "Returns videos from channels the user is subscribed to",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feed retrieved successfully",
                            content = @Content(schema = @Schema(implementation = FeedDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<FeedDto> getSubscriptionsFeed(
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer pageSize,
            @RequestParam(required = false) String pageToken,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            JwtAuthenticationToken authentication) {
        
        String userId = extractUserId(authentication);
        
        log.info("Getting subscriptions feed for userId={}, pageSize={}", userId, pageSize);
        
        FeedDto feed = getFeedUseCase.execute(userId, FeedType.SUBSCRIPTIONS, pageSize, pageToken);
        
        if (ifNoneMatch != null && ifNoneMatch.equals(feed.getEtag())) {
            return ResponseEntity.status(304)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                    .header(HttpHeaders.ETAG, feed.getEtag())
                    .build();
        }
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                .header(HttpHeaders.ETAG, feed.getEtag())
                .body(feed);
    }
    
    @GetMapping("/trending")
    @Operation(
            summary = "Get trending feed",
            description = "Returns trending videos across the platform",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Feed retrieved successfully",
                            content = @Content(schema = @Schema(implementation = FeedDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<FeedDto> getTrendingFeed(
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer pageSize,
            @RequestParam(required = false) String pageToken,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            JwtAuthenticationToken authentication) {
        
        String userId = extractUserId(authentication);
        
        log.info("Getting trending feed for userId={}, pageSize={}", userId, pageSize);
        
        FeedDto feed = getFeedUseCase.execute(userId, FeedType.TRENDING, pageSize, pageToken);
        
        if (ifNoneMatch != null && ifNoneMatch.equals(feed.getEtag())) {
            return ResponseEntity.status(304)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                    .header(HttpHeaders.ETAG, feed.getEtag())
                    .build();
        }
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                .header(HttpHeaders.ETAG, feed.getEtag())
                .body(feed);
    }
    
    private String extractUserId(JwtAuthenticationToken authentication) {
        return authentication.getToken().getClaim("sub"); // OIDC subject claim
    }
}

