package com.youtube.contentidservice.application.commands;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateFingerprintCommand {
    @NotNull
    UUID videoId;
    
    String blobUri; // Optional: if provided, use blob URI instead of videoId to fetch from blob storage
}

