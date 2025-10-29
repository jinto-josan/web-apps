package com.youtube.livestreaming.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class LiveEventSummaryDto {
    private String id;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("channelId")
    private String channelId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("startedAt")
    private Instant startedAt;
    
    @JsonProperty("isRunning")
    private Boolean isRunning;
}

