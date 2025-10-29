package com.youtube.livestreaming.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateLiveEventRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @NotBlank(message = "Channel ID is required")
    private String channelId;
    
    @JsonProperty("dvr")
    private DvrConfiguration dvr;
    
    @JsonProperty("lowLatency")
    private Boolean lowLatencyEnabled;
    
    @JsonProperty("maxConcurrentViewers")
    @Min(1)
    @Max(1000000)
    private Integer maxConcurrentViewers;
    
    private List<String> allowedCountries;
    private List<String> blockedCountries;
    
    @Data
    public static class DvrConfiguration {
        @JsonProperty("enabled")
        private Boolean enabled = true;
        
        @JsonProperty("windowInMinutes")
        @Min(1)
        @Max(1440)
        private Integer windowInMinutes = 120;
    }
}

