package com.youtube.contentidservice.application.commands;

import com.youtube.contentidservice.domain.valueobjects.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class ResolveClaimCommand {
    @NotNull
    UUID claimId;
    
    @NotNull
    DisputeStatus disputeStatus;
    
    String resolution; // Optional resolution notes
}

