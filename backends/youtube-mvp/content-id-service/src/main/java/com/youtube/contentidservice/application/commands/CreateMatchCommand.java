package com.youtube.contentidservice.application.commands;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateMatchCommand {
    @NotNull
    UUID sourceFingerprintId;
    
    @NotNull
    UUID matchedFingerprintId;
    
    @NotNull
    UUID sourceVideoId;
    
    @NotNull
    UUID matchedVideoId;
    
    double score; // 0.0 to 1.0
}

