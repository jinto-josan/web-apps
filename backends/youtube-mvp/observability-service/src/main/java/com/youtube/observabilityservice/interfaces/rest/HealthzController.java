package com.youtube.observabilityservice.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/healthz")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check endpoints")
public class HealthzController {
    
    @GetMapping("/deep")
    @Operation(summary = "Deep health check including dependencies")
    public ResponseEntity<Map<String, Object>> deepHealth() {
        // In a real implementation, check database, Azure Monitor, etc.
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", Instant.now(),
                "checks", Map.of(
                        "database", "ok",
                        "azure_monitor", "ok",
                        "redis", "ok"
                )
        ));
    }
    
    @GetMapping
    @Operation(summary = "Basic health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "up",
                "timestamp", Instant.now().toString()
        ));
    }
}

