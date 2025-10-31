package com.youtube.experimentationservice.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    
    private Counter featureFlagRequests;
    private Counter experimentRequests;
    private Timer featureFlagResolutionTime;
    private Timer experimentResolutionTime;
    
    public void init() {
        featureFlagRequests = Counter.builder("feature.flag.requests")
                .description("Total feature flag requests")
                .register(meterRegistry);
        
        experimentRequests = Counter.builder("experiment.requests")
                .description("Total experiment requests")
                .register(meterRegistry);
        
        featureFlagResolutionTime = Timer.builder("feature.flag.resolution.time")
                .description("Feature flag resolution time")
                .register(meterRegistry);
        
        experimentResolutionTime = Timer.builder("experiment.resolution.time")
                .description("Experiment resolution time")
                .register(meterRegistry);
    }
    
    public void incrementFeatureFlagRequests() {
        if (featureFlagRequests != null) {
            featureFlagRequests.increment();
        }
    }
    
    public void incrementExperimentRequests() {
        if (experimentRequests != null) {
            experimentRequests.increment();
        }
    }
    
    public Timer.Sample startFeatureFlagResolution() {
        return Timer.start(meterRegistry);
    }
    
    public Timer.Sample startExperimentResolution() {
        return Timer.start(meterRegistry);
    }
    
    public void recordFeatureFlagResolution(Timer.Sample sample) {
        if (featureFlagResolutionTime != null && sample != null) {
            sample.stop(featureFlagResolutionTime);
        }
    }
    
    public void recordExperimentResolution(Timer.Sample sample) {
        if (experimentResolutionTime != null && sample != null) {
            sample.stop(experimentResolutionTime);
        }
    }
}

