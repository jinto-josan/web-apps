package com.youtube.mvp.feeds.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.mvp.feeds.domain.model.FeedType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class FeedDto {
    List<FeedItemDto> items;
    Instant lastUpdated;
    String etag;
    
    @JsonProperty("totalCount")
    Long totalCount;
    
    @JsonProperty("pageSize")
    Integer pageSize;
    
    @JsonProperty("nextPageToken")
    String nextPageToken;
}

