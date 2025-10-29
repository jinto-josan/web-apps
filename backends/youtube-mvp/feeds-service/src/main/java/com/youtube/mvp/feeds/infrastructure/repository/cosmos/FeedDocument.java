package com.youtube.mvp.feeds.infrastructure.repository.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Container(containerName = "feeds")
public class FeedDocument {
    
    @org.springframework.data.annotation.Id
    private String id;
    
    @PartitionKey
    private String userId;
    
    private String feedType;
    
    private List<com.youtube.mvp.feeds.domain.model.FeedItem> items;
    
    private String itemsJson; // For serialized items
    
    private Instant lastUpdated;
    
    private String etag;
    
    private Long totalCount;
    
    private Integer pageSize;
    
    private String nextPageToken;
}

