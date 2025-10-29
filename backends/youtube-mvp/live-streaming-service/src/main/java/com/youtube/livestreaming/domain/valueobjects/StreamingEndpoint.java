package com.youtube.livestreaming.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

/**
 * Value object representing a streaming endpoint
 */
@Value
@Builder
public class StreamingEndpoint {
    String url;
    String protocol; // HLS, DASH, etc.
    String resolution;
    Integer bitrate;
    
    public boolean isHls() {
        return "HLS".equalsIgnoreCase(protocol);
    }
    
    public boolean isDash() {
        return "DASH".equalsIgnoreCase(protocol);
    }
}

