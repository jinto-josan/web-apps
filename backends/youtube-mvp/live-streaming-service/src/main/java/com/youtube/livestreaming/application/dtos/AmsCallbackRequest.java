package com.youtube.livestreaming.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Azure Media Services callback payload
 */
@Data
public class AmsCallbackRequest {
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("eventData")
    private EventData eventData;
    
    @Data
    public static class EventData {
        @JsonProperty("liveEventName")
        private String liveEventName;
        
        @JsonProperty("state")
        private String state;
        
        @JsonProperty("stateChangeTime")
        private String stateChangeTime;
        
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("ingestUrl")
        private String ingestUrl;
        
        @JsonProperty("previewUrl")
        private String previewUrl;
        
        @JsonProperty("resourceGroup")
        private String resourceGroup;
        
        @JsonProperty("accountName")
        private String accountName;
    }
}

