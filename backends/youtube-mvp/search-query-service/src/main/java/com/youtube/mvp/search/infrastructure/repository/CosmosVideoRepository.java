package com.youtube.mvp.search.infrastructure.repository;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Repository for accessing video documents from Cosmos DB.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CosmosVideoRepository {
    
    private final CosmosClient cosmosClient;
    private final ObjectMapper objectMapper;
    
    @Value("${azure.cosmos.database-name}")
    private String databaseName;
    
    @Value("${azure.cosmos.container-name}")
    private String containerName;
    
    private CosmosContainer container;
    
    @PostConstruct
    public void init() {
        CosmosDatabase database = cosmosClient.getDatabase(databaseName);
        this.container = database.getContainer(containerName);
    }
    
    public Map<String, Object> findVideoById(String videoId) {
        try {
            CosmosItemResponse<Map> response = container.readItem(
                    videoId,
                    new PartitionKey(videoId),
                    Map.class
            );
            return response.getItem();
        } catch (Exception e) {
            log.warn("Video not found: {}", videoId);
            return null;
        }
    }
    
    public List<Map<String, Object>> findAllVideos() {
        List<Map<String, Object>> videos = new ArrayList<>();
        String query = "SELECT * FROM c";
        
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        FeedResponse<Map> results = container.queryItems(querySpec, Map.class).getIterableByPage();
        
        for (FeedResponse<Map> page : results) {
            page.getResults().forEach(item -> videos.add(objectMapper.convertValue(item, Map.class)));
        }
        
        return videos;
    }
    
    public void upsertVideo(Map<String, Object> video) {
        container.upsertItem(video);
    }
    
    public void deleteVideo(String videoId) {
        container.deleteItem(videoId, new PartitionKey(videoId));
    }
}
