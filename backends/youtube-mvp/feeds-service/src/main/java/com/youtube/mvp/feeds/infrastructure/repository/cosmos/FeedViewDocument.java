package com.youtube.mvp.feeds.infrastructure.repository.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Container(containerName = "feed-views")
public class FeedViewDocument {
    
    @org.springframework.data.annotation.Id
    private String id;
    
    @PartitionKey
    private String userId;
    
    private String videoId;
    
    private String feedType;
    
    private Instant viewedAt;
    
    private Integer position;
    
    private String videoCategory;
    
    private Long videoViewCount;
    
    private String videoChannelId;
}

