package com.youtube.antiaabuseservice.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    
    private Counter riskScoreRequests;
    private Counter ruleEvaluationRequests;
    private Timer riskScoreCalculationTime;
    private Counter mlEndpointCalls;
    private Counter mlEndpointFailures;
    
    public void init() {
        riskScoreRequests = Counter.builder("risk.score.requests")
                .description("Total risk score requests")
                .register(meterRegistry);
        
        ruleEvaluationRequests = Counter.builder("rule.evaluation.requests")
                .description("Total rule evaluation requests")
                .register(meterRegistry);
        
        riskScoreCalculationTime = Timer.builder("risk.score.calculation.time")
                .description("Risk score calculation time")
                .register(meterRegistry);
        
        mlEndpointCalls = Counter.builder("ml.endpoint.calls")
                .description("ML endpoint calls")
                .register(meterRegistry);
        
        mlEndpointFailures = Counter.builder("ml.endpoint.failures")
                .description("ML endpoint failures")
                .register(meterRegistry);
    }
    
    public void incrementRiskScoreRequests() {
        if (riskScoreRequests != null) {
            riskScoreRequests.increment();
        }
    }
    
    public void incrementRuleEvaluationRequests() {
        if (ruleEvaluationRequests != null) {
            ruleEvaluationRequests.increment();
        }
    }
    
    public void incrementMlEndpointCalls() {
        if (mlEndpointCalls != null) {
            mlEndpointCalls.increment();
        }
    }
    
    public void incrementMlEndpointFailures() {
        if (mlEndpointFailures != null) {
            mlEndpointFailures.increment();
        }
    }
    
    public Timer.Sample startRiskScoreCalculation() {
        return Timer.start(meterRegistry);
    }
    
    public void recordRiskScoreCalculation(Timer.Sample sample) {
        if (riskScoreCalculationTime != null && sample != null) {
            sample.stop(riskScoreCalculationTime);
        }
    }
}

