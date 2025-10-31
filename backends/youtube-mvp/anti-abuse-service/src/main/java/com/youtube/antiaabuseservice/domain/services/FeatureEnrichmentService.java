package com.youtube.antiaabuseservice.domain.services;

import com.youtube.antiaabuseservice.domain.model.FeatureStore;
import com.youtube.antiaabuseservice.domain.model.RiskEvent;

import java.util.Map;

public interface FeatureEnrichmentService {
    /**
     * Enrich risk event with features from feature store and real-time sources.
     */
    Map<String, Object> enrichFeatures(RiskEvent event, FeatureStore featureStore);
}

