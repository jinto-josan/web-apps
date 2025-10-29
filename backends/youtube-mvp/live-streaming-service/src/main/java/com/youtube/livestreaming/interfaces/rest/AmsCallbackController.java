package com.youtube.livestreaming.interfaces.rest;

import com.youtube.livestreaming.application.dtos.AmsCallbackRequest;
import com.youtube.livestreaming.application.services.LiveEventOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling Azure Media Services callbacks
 */
@RestController
@RequestMapping("/api/v1/events/ams-live-callback")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AMS Callbacks", description = "Azure Media Services callback handlers")
public class AmsCallbackController {
    
    private final LiveEventOrchestrationService orchestrationService;
    
    @PostMapping
    @Operation(summary = "Handle AMS live event state change callback")
    public ResponseEntity<Void> handleAmsCallback(@RequestBody AmsCallbackRequest request) {
        log.info("Received AMS callback: eventType={}, liveEventName={}, state={}",
            request.getEventType(),
            request.getEventData() != null ? request.getEventData().getLiveEventName() : "unknown",
            request.getEventData() != null ? request.getEventData().getState() : "unknown");
        
        if (request.getEventData() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        var eventData = request.getEventData();
        String liveEventId = extractLiveEventId(eventData.getLiveEventName());
        String state = eventData.getState();
        
        try {
            orchestrationService.handleAmsCallback(liveEventId, state);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to process AMS callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String extractLiveEventId(String liveEventName) {
        // AMS live event name format: "live-{uuid}"
        // Extract the UUID part to match with our internal ID
        if (liveEventName != null && liveEventName.startsWith("live-")) {
            return liveEventName;
        }
        return liveEventName;
    }
}

