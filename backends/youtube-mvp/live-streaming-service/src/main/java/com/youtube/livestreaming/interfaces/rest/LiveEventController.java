package com.youtube.livestreaming.interfaces.rest;

import com.youtube.livestreaming.application.dtos.*;
import com.youtube.livestreaming.application.mappers.LiveEventMapper;
import com.youtube.livestreaming.application.services.LiveEventOrchestrationService;
import com.youtube.livestreaming.domain.entities.LiveEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for Live Events
 */
@RestController
@RequestMapping("/api/v1/live/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Live Events", description = "Live streaming event management API")
public class LiveEventController {
    
    private final LiveEventOrchestrationService orchestrationService;
    private final LiveEventMapper mapper;
    
    @PostMapping
    @Operation(summary = "Create a new live event")
    public ResponseEntity<LiveEventResponse> createLiveEvent(
        @Valid @RequestBody CreateLiveEventRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        log.info("Creating live event for user: {}", userId);
        
        var liveEvent = orchestrationService.createLiveEvent(request, userId, idempotencyKey);
        var response = mapper.toResponse(liveEvent);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .eTag(liveEvent.getId())
            .body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a live event by ID")
    public ResponseEntity<LiveEventResponse> getLiveEvent(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        log.info("Getting live event {} for user: {}", id, userId);
        
        var liveEvent = orchestrationService.getLiveEvent(id, userId);
        var response = mapper.toResponse(liveEvent);
        
        return ResponseEntity.ok()
            .eTag(liveEvent.getId())
            .body(response);
    }
    
    @PostMapping("/{id}/start")
    @Operation(summary = "Start a live event")
    public ResponseEntity<LiveEventResponse> startLiveEvent(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        log.info("Starting live event {} for user: {}", id, userId);
        
        var liveEvent = orchestrationService.startLiveEvent(id, userId);
        var response = mapper.toResponse(liveEvent);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/stop")
    @Operation(summary = "Stop a live event")
    public ResponseEntity<LiveEventResponse> stopLiveEvent(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        log.info("Stopping live event {} for user: {}", id, userId);
        
        var liveEvent = orchestrationService.stopLiveEvent(id, userId);
        var response = mapper.toResponse(liveEvent);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/channel/{channelId}")
    @Operation(summary = "Get live events by channel")
    public ResponseEntity<List<LiveEventSummaryDto>> getLiveEventsByChannel(
        @PathVariable String channelId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Getting live events for channel: {}", channelId);
        
        var liveEvents = orchestrationService.getLiveEventsByChannel(channelId);
        var summaries = liveEvents.stream()
            .map(mapper::toSummaryDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(summaries);
    }
    
    @GetMapping("/my-events")
    @Operation(summary = "Get current user's live events")
    public ResponseEntity<List<LiveEventSummaryDto>> getMyLiveEvents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        log.info("Getting live events for user: {}", userId);
        
        var liveEvents = orchestrationService.getLiveEventsByUser(userId);
        var summaries = liveEvents.stream()
            .map(mapper::toSummaryDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(summaries);
    }
}

