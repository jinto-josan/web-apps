package com.youtube.contentidservice.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class MatchRequest {
    @NotNull
    UUID fingerprintId;
    
    double threshold; // Optional, defaults to 0.7
}

