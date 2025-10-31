package com.youtube.observabilityservice.domain.services;

import com.youtube.observabilityservice.domain.entities.SLI;
import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.valueobjects.SLOId;
import com.youtube.observabilityservice.domain.valueobjects.TimeWindow;
import com.youtube.observabilityservice.infrastructure.services.DefaultSLOCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSLOCalculatorTest {
    
    private DefaultSLOCalculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new DefaultSLOCalculator();
    }
    
    @Test
    void testCalculateSLO_WithValidSLIs() {
        SLO slo = SLO.builder()
                .id(SLOId.random())
                .name("Test SLO")
                .serviceName("test-service")
                .targetPercent(99.9)
                .timeWindow(TimeWindow.rollingDays(30))
                .build();
        
        List<SLI> slis = Arrays.asList(
                SLI.builder().name("availability").lastValue(99.95).build(),
                SLI.builder().name("latency").lastValue(99.85).build()
        );
        
        double result = calculator.calculateSLO(slo, slis);
        
        assertEquals(99.9, result, 0.01);
    }
    
    @Test
    void testCalculateSLO_WithEmptySLIs() {
        SLO slo = SLO.builder()
                .id(SLOId.random())
                .name("Test SLO")
                .serviceName("test-service")
                .targetPercent(99.9)
                .timeWindow(TimeWindow.rollingDays(30))
                .build();
        
        double result = calculator.calculateSLO(slo, Collections.emptyList());
        
        assertEquals(0.0, result);
    }
    
    @Test
    void testCalculateErrorBudgetBurnRate() {
        SLO slo = SLO.builder()
                .id(SLOId.random())
                .name("Test SLO")
                .serviceName("test-service")
                .targetPercent(99.9)
                .timeWindow(TimeWindow.rollingDays(30))
                .build();
        
        double burnRate = calculator.calculateErrorBudgetBurnRate(slo, 99.8, 99.9);
        
        assertTrue(burnRate > 0);
    }
    
    @Test
    void testCalculateRemainingErrorBudget() {
        SLO slo = SLO.builder()
                .id(SLOId.random())
                .name("Test SLO")
                .serviceName("test-service")
                .targetPercent(99.9)
                .timeWindow(TimeWindow.rollingDays(30))
                .build();
        
        double remaining = calculator.calculateRemainingErrorBudget(slo, 99.95);
        
        assertTrue(remaining >= 0);
        assertTrue(remaining <= 0.1); // Error budget is 0.1%
    }
}

