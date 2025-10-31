package com.youtube.analyticstelemetryservice.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking telemetry statistics.
 * Thread-safe counters for metrics.
 */
@Slf4j
@Service
public class TelemetryStatsService {
    
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);
    private final AtomicLong errorsCount = new AtomicLong(0);
    private final AtomicLong dlqCount = new AtomicLong(0);
    private final Map<String, AtomicLong> eventsByType = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> eventsBySource = new ConcurrentHashMap<>();
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
    
    public void recordProcessed(int count) {
        totalEventsProcessed.addAndGet(count);
    }
    
    public void recordError(int count) {
        errorsCount.addAndGet(count);
    }
    
    public void recordDlq(int count) {
        dlqCount.addAndGet(count);
    }
    
    public void recordEventType(String eventType) {
        eventsByType.computeIfAbsent(eventType, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public void recordEventSource(String eventSource) {
        eventsBySource.computeIfAbsent(eventSource, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    public long getTotalEventsProcessed() {
        return totalEventsProcessed.get();
    }
    
    public double getEventsPerSecond() {
        long elapsed = (System.currentTimeMillis() - startTime.get()) / 1000;
        return elapsed > 0 ? (double) totalEventsProcessed.get() / elapsed : 0.0;
    }
    
    public Map<String, Long> getEventsByType() {
        return eventsByType.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get()
            ));
    }
    
    public Map<String, Long> getEventsBySource() {
        return eventsBySource.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get()
            ));
    }
    
    public long getErrorsCount() {
        return errorsCount.get();
    }
    
    public long getDlqCount() {
        return dlqCount.get();
    }
}

