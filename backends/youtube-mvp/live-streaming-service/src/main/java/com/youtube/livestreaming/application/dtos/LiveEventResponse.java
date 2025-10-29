package com.youtube.livestreaming.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class LiveEventResponse {
    private String id;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("channelId")
    private String channelId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("ingestUrl")
    private String ingestUrl;
    
    @JsonProperty("previewUrl")
    private String previewUrl;
    
    @JsonProperty("watchUrl")
    private String watchUrl;
    
    @JsonProperty("streamingEndpoints")
    private List<StreamingEndpointDto> streamingEndpoints;
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("startedAt")
    private Instant startedAt;
    
    @JsonProperty("stoppedAt")
    private Instant stoppedAt;
    
    @JsonProperty("isRunning")
    private Boolean isRunning;
    
    @JsonProperty("dvr")
    private DvrInfo dvr;
    
    @JsonProperty("concurrentViewers")
    private Integer concurrentViewers;
    
    @Data
    public static class StreamingEndpointDto {
        private String url;
        private String protocol;
        private String resolution;
        private Integer bitrate;
    }
    
    @Data
    public static class DvrInfo {
        private Boolean enabled;
        private Integer windowInMinutes;
    }
}

