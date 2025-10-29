package com.youtube.mvp.feeds.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class FeedItemDto {
    @NotBlank
    String videoId;
    
    @NotBlank
    String title;
    
    @NotBlank
    String channelId;
    
    @NotBlank
    String channelName;
    
    @NotBlank
    String thumbnailUrl;
    
    @NotNull
    Instant publishedAt;
    
    @JsonProperty("viewCount")
    Long viewCount;
    
    @JsonProperty("durationSeconds")
    Long durationSeconds;
    
    @JsonProperty("isAd")
    boolean isAd;
    
    @JsonProperty("adSlotIndex")
    Integer adSlotIndex;
    
    String category;
}

