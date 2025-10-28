package com.youtube.channelservice.interfaces.rest;

import com.youtube.channelservice.application.commands.SubscribeToChannelCommand;
import com.youtube.channelservice.application.commands.UnsubscribeFromChannelCommand;
import com.youtube.channelservice.application.queries.GetChannelSubscriptionStatsQuery;
import com.youtube.channelservice.application.queries.GetUserSubscriptionsQuery;
import com.youtube.channelservice.application.usecases.SubscriptionUseCase;
import com.youtube.channelservice.domain.models.ChannelSubscriptionStats;
import com.youtube.channelservice.domain.models.Subscription;
import com.youtube.channelservice.domain.models.UserSubscriptionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Subscriptions", description = "Channel subscription management API")
@Validated
public class SubscriptionController {
    
    private final SubscriptionUseCase subscriptionUseCase;
    
    public SubscriptionController(SubscriptionUseCase subscriptionUseCase) {
        this.subscriptionUseCase = subscriptionUseCase;
    }
    
    @PostMapping("/channels/{channelId}/subscriptions")
    @Operation(summary = "Subscribe to a channel", 
               description = "Subscribe the authenticated user to a channel. Requires Idempotency-Key header.")
    @ApiResponse(responseCode = "201", description = "Successfully subscribed")
    @ApiResponse(responseCode = "409", description = "Already subscribed")
    @ApiResponse(responseCode = "400", description = "Bad request")
    public ResponseEntity<Subscription> subscribe(
            @Parameter(description = "Channel ID to subscribe to") 
            @PathVariable @NotBlank String channelId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader(value = "Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestBody(required = false) SubscribeRequest request) {
        
        String userId = extractUserId(jwt);
        
        SubscribeToChannelCommand command = SubscribeToChannelCommand.builder()
            .userId(userId)
            .channelId(channelId)
            .idempotencyKey(idempotencyKey)
            .notifyOnUpload(request != null ? request.notifyOnUpload : true)
            .notifyOnLive(request != null ? request.notifyOnLive : true)
            .notifyOnCommunityPost(request != null ? request.notifyOnCommunityPost : true)
            .notifyOnShorts(request != null ? request.notifyOnShorts : true)
            .build();
        
        Subscription subscription = subscriptionUseCase.subscribeToChannel(command);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("ETag", "\"" + subscription.getId() + "\"")
            .body(subscription);
    }
    
    @DeleteMapping("/channels/{channelId}/subscriptions")
    @Operation(summary = "Unsubscribe from a channel",
               description = "Unsubscribe the authenticated user from a channel. Requires Idempotency-Key header.")
    @ApiResponse(responseCode = "204", description = "Successfully unsubscribed")
    @ApiResponse(responseCode = "404", description = "Subscription not found")
    public ResponseEntity<Void> unsubscribe(
            @Parameter(description = "Channel ID to unsubscribe from")
            @PathVariable @NotBlank String channelId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestBody(required = false) UnsubscribeRequest request) {
        
        String userId = extractUserId(jwt);
        
        UnsubscribeFromChannelCommand command = UnsubscribeFromChannelCommand.builder()
            .userId(userId)
            .channelId(channelId)
            .idempotencyKey(idempotencyKey)
            .reason(request != null ? request.reason : null)
            .build();
        
        subscriptionUseCase.unsubscribeFromChannel(command);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/users/{userId}/subscriptions")
    @Operation(summary = "Get user subscriptions",
               description = "Get all active subscriptions for a user with pagination support.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved subscriptions")
    public ResponseEntity<UserSubscriptionInfo> getUserSubscriptions(
            @Parameter(description = "User ID")
            @PathVariable @NotBlank String userId,
            @Parameter(description = "Pagination offset")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Pagination limit (max 100)")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Include inactive subscriptions")
            @RequestParam(required = false) Boolean includeInactive) {
        
        if (limit > 100) {
            limit = 100;
        }
        
        GetUserSubscriptionsQuery query = GetUserSubscriptionsQuery.builder()
            .userId(userId)
            .offset(offset)
            .limit(limit)
            .includeInactive(includeInactive)
            .build();
        
        UserSubscriptionInfo info = subscriptionUseCase.getUserSubscriptions(query);
        
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/channels/{channelId}/subscription-stats")
    @Operation(summary = "Get channel subscription statistics",
               description = "Get subscription count and statistics for a channel.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved stats")
    public ResponseEntity<ChannelSubscriptionStats> getChannelSubscriptionStats(
            @Parameter(description = "Channel ID")
            @PathVariable @NotBlank String channelId) {
        
        GetChannelSubscriptionStatsQuery query = GetChannelSubscriptionStatsQuery.builder()
            .channelId(channelId)
            .build();
        
        ChannelSubscriptionStats stats = subscriptionUseCase.getChannelSubscriptionStats(query);
        
        return ResponseEntity.ok(stats);
    }
    
    private String extractUserId(Jwt jwt) {
        String uid = jwt.getClaimAsString("uid");
        return (uid != null && !uid.isBlank()) ? uid : jwt.getSubject();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscribeRequest {
        private Boolean notifyOnUpload;
        private Boolean notifyOnLive;
        private Boolean notifyOnCommunityPost;
        private Boolean notifyOnShorts;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnsubscribeRequest {
        private String reason;
    }
}
