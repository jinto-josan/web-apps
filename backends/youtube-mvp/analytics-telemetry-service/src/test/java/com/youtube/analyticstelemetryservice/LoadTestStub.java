package com.youtube.analyticstelemetryservice;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH-based load test stub for telemetry event processing.
 * Example load test to measure event processing throughput.
 * 
 * Run with: mvn test-compile exec:java -Dexec.mainClass="com.youtube.analyticstelemetryservice.LoadTestStub"
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class LoadTestStub {
    
    private List<com.youtube.analyticstelemetryservice.application.dto.TelemetryEventRequest> events;
    
    @Setup(Level.Trial)
    public void setup() {
        events = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            com.youtube.analyticstelemetryservice.application.dto.TelemetryEventRequest event = 
                new com.youtube.analyticstelemetryservice.application.dto.TelemetryEventRequest();
            event.setEventType("video.view");
            event.setEventSource("web");
            event.setTimestamp(Instant.now());
            event.setUserId("user-" + i);
            event.setSessionId("session-" + i);
            events.add(event);
        }
    }
    
    @Benchmark
    public void processEvents() {
        // This is a stub - in a real scenario, this would call the actual service
        // TelemetryApplicationService.processBatch(batchRequest)
        
        // Simulate processing
        for (com.youtube.analyticstelemetryservice.application.dto.TelemetryEventRequest event : events) {
            // Simulate validation
            if (event.getEventType() == null) {
                continue;
            }
            // Simulate serialization
            String json = event.toString();
        }
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(LoadTestStub.class.getSimpleName())
            .forks(1)
            .warmupIterations(5)
            .measurementIterations(10)
            .build();
        
        new Runner(opt).run();
    }
}

