package com.youtube.livestreaming.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

/**
 * Azure Media Services Live Event reference
 */
@Value
@Builder
public class AmsLiveEventReference {
    String liveEventId;
    String liveEventName;
    String resourceGroupName;
    String accountName;
    String resourceId;
    String ingestUrl;
    String previewUrl;
    String state;
    
    public boolean isRunning() {
        return "Running".equalsIgnoreCase(state);
    }
}

