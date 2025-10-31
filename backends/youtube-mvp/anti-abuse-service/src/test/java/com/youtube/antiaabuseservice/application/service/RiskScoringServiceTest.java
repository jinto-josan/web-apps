package com.youtube.antiaabuseservice.application.service;

import com.youtube.antiaabuseservice.application.dto.RiskScoreRequest;
import com.youtube.antiaabuseservice.application.dto.RiskScoreResponse;
import com.youtube.antiaabuseservice.application.mappers.AntiAbuseMapper;
import com.youtube.antiaabuseservice.application.mappers.AntiAbuseMapperImpl;
import com.youtube.antiaabuseservice.domain.model.FeatureStore;
import com.youtube.antiaabuseservice.domain.model.RiskEvent;
import com.youtube.antiaabuseservice.domain.model.RiskScore;
import com.youtube.antiaabuseservice.domain.repositories.FeatureStoreRepository;
import com.youtube.antiaabuseservice.domain.repositories.RuleRepository;
import com.youtube.antiaabuseservice.domain.services.FeatureEnrichmentService;
import com.youtube.antiaabuseservice.domain.services.RiskEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskScoringServiceTest {

    @Mock
    private RiskEngine riskEngine;

    @Mock
    private FeatureEnrichmentService featureEnrichmentService;

    @Mock
    private FeatureStoreRepository featureStoreRepository;

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private AntiAbuseMapper mapper;

    @InjectMocks
    private RiskScoringService riskScoringService;

    @BeforeEach
    void setUp() {
        // Setup default mocks
        when(ruleRepository.findAllEnabled()).thenReturn(Collections.emptyList());
    }

    @Test
    void testCalculateRiskScore_Success() {
        RiskScoreRequest request = RiskScoreRequest.builder()
                .eventType("VIEW")
                .userId("user123")
                .contentId("video456")
                .context(Map.of("ipAddress", "192.168.1.1"))
                .build();

        FeatureStore featureStore = FeatureStore.builder()
                .userId("user123")
                .featureSet("risk-features")
                .features(Map.of("totalEvents", 10))
                .lastUpdated(Instant.now())
                .build();

        RiskScore riskScore = RiskScore.builder()
                .eventId("event-1")
                .userId("user123")
                .score(0.5)
                .riskLevel(RiskScore.RiskLevel.MEDIUM)
                .triggeredRules(Collections.emptyList())
                .recommendedAction(RiskScore.EnforcementAction.ALLOW)
                .timestamp(Instant.now())
                .latencyMs(45L)
                .build();

        RiskScoreResponse response = RiskScoreResponse.builder()
                .eventId("event-1")
                .score(0.5)
                .riskLevel("MEDIUM")
                .recommendedAction("ALLOW")
                .latencyMs(45L)
                .build();

        when(featureStoreRepository.findByUserIdAndFeatureSet("user123", "risk-features"))
                .thenReturn(Optional.of(featureStore));
        when(featureEnrichmentService.enrichFeatures(any(), any()))
                .thenReturn(Map.of("totalEvents", 10, "eventType", "VIEW"));
        when(riskEngine.calculateRisk(any(), any(), any())).thenReturn(riskScore);
        when(mapper.toResponse(any(RiskScore.class))).thenReturn(response);

        RiskScoreResponse result = riskScoringService.calculateRiskScore(request);

        assertNotNull(result);
        assertEquals(0.5, result.getScore());
        assertEquals("MEDIUM", result.getRiskLevel());
        verify(riskEngine, times(1)).calculateRisk(any(), any(), any());
    }

    @Test
    void testCalculateRiskScore_NoFeatureStore() {
        RiskScoreRequest request = RiskScoreRequest.builder()
                .eventType("VIEW")
                .userId("user123")
                .build();

        when(featureStoreRepository.findByUserIdAndFeatureSet("user123", "risk-features"))
                .thenReturn(Optional.empty());
        when(featureEnrichmentService.enrichFeatures(any(), any()))
                .thenReturn(Map.of("totalEvents", 0));
        when(riskEngine.calculateRisk(any(), any(), any())).thenReturn(
                RiskScore.builder()
                        .score(0.0)
                        .riskLevel(RiskScore.RiskLevel.LOW)
                        .recommendedAction(RiskScore.EnforcementAction.ALLOW)
                        .build()
        );
        when(mapper.toResponse(any(RiskScore.class))).thenReturn(
                RiskScoreResponse.builder()
                        .score(0.0)
                        .riskLevel("LOW")
                        .recommendedAction("ALLOW")
                        .build()
        );

        RiskScoreResponse result = riskScoringService.calculateRiskScore(request);

        assertNotNull(result);
        verify(featureStoreRepository, times(1)).save(any(FeatureStore.class));
    }

    @Test
    void testCalculateRiskScore_ExceptionHandling() {
        RiskScoreRequest request = RiskScoreRequest.builder()
                .eventType("VIEW")
                .userId("user123")
                .build();

        when(featureStoreRepository.findByUserIdAndFeatureSet(any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        RiskScoreResponse result = riskScoringService.calculateRiskScore(request);

        assertNotNull(result);
        assertEquals(0.0, result.getScore());
        assertEquals("LOW", result.getRiskLevel());
        assertEquals("ALLOW", result.getRecommendedAction());
    }
}

