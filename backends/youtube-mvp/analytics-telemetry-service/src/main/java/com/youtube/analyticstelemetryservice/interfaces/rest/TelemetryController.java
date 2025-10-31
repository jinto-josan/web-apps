package com.youtube.analyticstelemetryservice.interfaces.rest;

import com.youtube.analyticstelemetryservice.application.dto.BatchEventRequest;
import com.youtube.analyticstelemetryservice.application.dto.BatchEventResponse;
import com.youtube.analyticstelemetryservice.application.dto.StatsResponse;
import com.youtube.analyticstelemetryservice.application.service.TelemetryApplicationService;
import com.youtube.analyticstelemetryservice.application.service.TelemetryStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for telemetry event collection.
 * Provides batch event ingestion endpoint with API versioning.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Telemetry Events", description = "API for collecting telemetry events")
public class TelemetryController {
    
    private final TelemetryApplicationService applicationService;
    private final TelemetryStatsService statsService;
    
    @PostMapping("/batch")
    @Operation(summary = "Submit batch of telemetry events", 
               description = "Accepts up to 1000 events per batch. Events are validated, deduplicated, and forwarded to Event Hubs.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Events accepted for processing"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAuthority('SCOPE_telemetry.write')")
    public CompletableFuture<ResponseEntity<BatchEventResponse>> collectEvents(
            @Valid @RequestBody BatchEventRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        log.info("Received batch request with {} events", request.getEvents().size());
        
        // Use header idempotency key if provided and not in body
        if (request.getIdempotencyKey() == null && idempotencyKey != null) {
            request.setIdempotencyKey(idempotencyKey);
        }
        
        return applicationService.processBatch(request)
            .thenApply(response -> {
                HttpStatus status = response.getTotalAccepted() > 0 
                    ? HttpStatus.ACCEPTED 
                    : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status).body(response);
            });
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get service statistics", 
               description = "Returns current statistics about event processing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @PreAuthorize("hasAuthority('SCOPE_telemetry.read')")
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = StatsResponse.builder()
            .totalEventsProcessed(statsService.getTotalEventsProcessed())
            .eventsPerSecond(statsService.getEventsPerSecond())
            .publisherHealthy(applicationService.isHealthy())
            .backpressureActive(false) // TODO: Implement backpressure detection
            .eventsByType(statsService.getEventsByType())
            .eventsBySource(statsService.getEventsBySource())
            .errorsCount(statsService.getErrorsCount())
            .dlqCount(statsService.getDlqCount())
            .lastUpdated(Instant.now())
            .build();
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<String> health() {
        boolean healthy = applicationService.isHealthy();
        return healthy 
            ? ResponseEntity.ok("UP")
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DOWN");
    }
}

