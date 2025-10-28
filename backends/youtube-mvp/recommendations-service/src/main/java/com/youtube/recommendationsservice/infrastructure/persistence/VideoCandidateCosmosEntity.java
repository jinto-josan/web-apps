package com.youtube.recommendationsservice.infrastructure.persistence;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Container(containerName = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoCandidateCosmosEntity {
    
    @org.springframework.data.annotation.Id
    @PartitionKey
    private String videoId;
    private String title;
    private String category;
    private List<String> tags;
    private Instant publishedAt;
    
    // Feature vectors
    private List<Double> embeddings;
    private Map<String, String> categoricalFeatures;
    private Map<String, Double> numericalFeatures;
    
    private Map<String, Object> metadata;
}

