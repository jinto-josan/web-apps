package com.youtube.antiaabuseservice.domain.services.impl;

import com.youtube.antiaabuseservice.domain.model.FeatureStore;
import com.youtube.antiaabuseservice.domain.model.RiskEvent;
import com.youtube.antiaabuseservice.domain.services.FeatureEnrichmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FeatureEnrichmentServiceImpl implements FeatureEnrichmentService {

    @Override
    @Cacheable(value = "features", key = "#event.userId + ':' + #event.eventType")
    public Map<String, Object> enrichFeatures(RiskEvent event, FeatureStore featureStore) {
        Map<String, Object> features = new HashMap<>(featureStore.getFeatures());
        
        // Add event-specific features
        features.put("eventType", event.getEventType().name());
        features.put("eventTimestamp", event.getTimestamp().toEpochMilli());
        
        // Add time-based features
        long hourOfDay = Instant.now().atZone(java.time.ZoneId.systemDefault()).getHour();
        features.put("hourOfDay", hourOfDay);
        features.put("dayOfWeek", Instant.now().atZone(java.time.ZoneId.systemDefault()).getDayOfWeek().getValue());
        
        // Add context features
        if (event.getContext() != null) {
            event.getContext().forEach((key, value) -> {
                if (value instanceof Number || value instanceof String || value instanceof Boolean) {
                    features.put("context_" + key, value);
                }
            });
        }
        
        // Historical features from feature store
        features.put("lastEventTime", featureStore.getLastUpdated().toEpochMilli());
        
        // Default values for missing features
        features.putIfAbsent("totalEvents", 0);
        features.putIfAbsent("riskHistory", 0.0);
        
        return features;
    }
}

