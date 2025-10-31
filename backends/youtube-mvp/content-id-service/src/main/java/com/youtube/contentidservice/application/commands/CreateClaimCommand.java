package com.youtube.contentidservice.application.commands;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class CreateClaimCommand {
    @NotNull
    UUID claimedVideoId;
    
    @NotNull
    UUID ownerId;
    
    @NotEmpty
    List<UUID> matchIds;
}

