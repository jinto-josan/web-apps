package com.youtube.channelservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents channel policy settings.
 * Immutable value object containing age gate and region block settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    
    private boolean ageGate;
    
    @Builder.Default
    private List<String> regionBlocks = List.of();
}