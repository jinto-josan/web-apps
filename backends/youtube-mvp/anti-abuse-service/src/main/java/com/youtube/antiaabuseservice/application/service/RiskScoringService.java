package com.youtube.antiaabuseservice.application.service;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.antiaabuseservice.application.dto.RiskScoreRequest;
import com.youtube.antiaabuseservice.application.dto.RiskScoreResponse;
import com.youtube.antiaabuseservice.application.mappers.AntiAbuseMapper;
import com.youtube.antiaabuseservice.domain.model.FeatureStore;
import com.youtube.antiaabuseservice.domain.model.RiskEvent;
import com.youtube.antiaabuseservice.domain.model.RiskScore;
import com.youtube.antiaabuseservice.domain.model.Rule;
import com.youtube.antiaabuseservice.domain.repositories.FeatureStoreRepository;
import com.youtube.antiaabuseservice.domain.repositories.RuleRepository;
import com.youtube.antiaabuseservice.domain.services.FeatureEnrichmentService;
import com.youtube.antiaabuseservice.domain.services.RiskEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoringService {
    private final RiskEngine riskEngine;
    private final FeatureEnrichmentService featureEnrichmentService;
    private final FeatureStoreRepository featureStoreRepository;
    private final RuleRepository ruleRepository;
    private final AntiAbuseMapper mapper;

    @Transactional
    public RiskScoreResponse calculateRiskScore(RiskScoreRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            RiskEvent event = mapper.toDomain(request);
            
            // Get feature store
            FeatureStore featureStore = featureStoreRepository
                    .findByUserIdAndFeatureSet(request.getUserId(), "risk-features")
                    .orElse(FeatureStore.builder()
                            .userId(request.getUserId())
                            .featureSet("risk-features")
                            .features(Map.of())
                            .lastUpdated(Instant.now())
                            .build());
            
            // Enrich features
            Map<String, Object> enrichedFeatures = featureEnrichmentService.enrichFeatures(event, featureStore);
            
            // Get enabled rules
            List<Rule> rules = ruleRepository.findAllEnabled();
            
            // Calculate risk score
            RiskScore riskScore = riskEngine.calculateRisk(event, enrichedFeatures, rules);
            riskScore.setLatencyMs(System.currentTimeMillis() - startTime);
            
            // Update feature store if needed
            if (!featureStore.getFeatures().equals(enrichedFeatures)) {
                featureStore.setFeatures(enrichedFeatures);
                featureStore.setLastUpdated(Instant.now());
                featureStoreRepository.save(featureStore);
            }
            
            return mapper.toResponse(riskScore);
            
        } catch (Exception e) {
            log.error("Error calculating risk score", e);
            // Return fallback score
            return RiskScoreResponse.builder()
                    .eventId(UlidCreator.getUlid().toString())
                    .score(0.0)
                    .riskLevel("LOW")
                    .recommendedAction("ALLOW")
                    .latencyMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
}

