package com.youtube.mvp.videocatalog.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    
    @JsonProperty("id")
    private String videoId;
    
    private String title;
    private String description;
    
    @JsonProperty("channelId")
    private String channelId;
    
    @JsonProperty("ownerId")
    private String ownerId;
    
    private String state;
    private String visibility;
    
    private List<LocalizedTextDto> localizedTitles;
    private List<LocalizedTextDto> localizedDescriptions;
    
    private List<String> tags;
    private String category;
    private String language;
    
    private String thumbnailUrl;
    private String contentUrl;
    
    private DurationDto duration;
    
    @JsonProperty("viewCount")
    private long viewCount;
    
    @JsonProperty("likeCount")
    private long likeCount;
    
    @JsonProperty("commentCount")
    private long commentCount;
    
    private String version; // ETag
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("updatedAt")
    private Instant updatedAt;
    
    @JsonProperty("publishedAt")
    private Instant publishedAt;
}

