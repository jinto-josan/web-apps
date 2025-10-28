package com.youtube.mvp.streaming.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    
    private String token;
    
    @JsonProperty("expiresAt")
    private Instant expiresAt;
    
    @JsonProperty("videoId")
    private String videoId;
    
    private String type; // playback, download, preview
}

