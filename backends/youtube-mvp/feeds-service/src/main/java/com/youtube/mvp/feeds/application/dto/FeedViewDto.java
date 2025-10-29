package com.youtube.mvp.feeds.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class FeedViewDto {
    String id;
    
    @JsonProperty("videoId")
    String videoId;
    
    @JsonProperty("userId")
    String userId;
    
    @JsonProperty("feedType")
    String feedType;
    
    @JsonProperty("viewedAt")
    Instant viewedAt;
    
    @JsonProperty("position")
    Integer position;
    
    String videoCategory;
    Long videoViewCount;
    String videoChannelId;
}

