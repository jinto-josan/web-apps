package com.youtube.recommendationsservice.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class FeatureVector {
    List<Double> embeddings;
    Map<String, Double> categoricalFeatures;
    Map<String, Double> numericalFeatures;
    
    public static FeatureVector empty() {
        return FeatureVector.builder()
            .embeddings(List.of())
            .categoricalFeatures(Map.of())
            .numericalFeatures(Map.of())
            .build();
    }
}

