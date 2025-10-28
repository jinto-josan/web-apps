package com.youtube.mvp.videocatalog.infrastructure.persistence.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;

/**
 * Cosmos DB entity for Video.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Container(containerName = "videos")
public class VideoCosmosEntity {
    
    @Id
    private String videoId;
    
    @PartitionKey
    private String partitionKey; // channelId for partitioning
    
    private String title;
    private String description;
    private String channelId;
    private String ownerId;
    
    private String state;
    private String visibility;
    
    private List<LocalizedTextEmbedded> titles;
    private List<LocalizedTextEmbedded> descriptions;
    private List<String> tags;
    private String category;
    private String language;
    private String thumbnailUrl;
    private String contentUrl;
    
    private Long durationSeconds;
    
    @JsonProperty("viewCount")
    private Long viewCount;
    
    @JsonProperty("likeCount")
    private Long likeCount;
    
    @JsonProperty("commentCount")
    private Long commentCount;
    
    private String version;
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("updatedAt")
    private Instant updatedAt;
    
    @JsonProperty("publishedAt")
    private Instant publishedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocalizedTextEmbedded {
        private String language;
        private String text;
    }
}

