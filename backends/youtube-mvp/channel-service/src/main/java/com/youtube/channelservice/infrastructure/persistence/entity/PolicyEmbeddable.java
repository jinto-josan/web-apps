package com.youtube.channelservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import java.util.List;

/**
 * Embeddable class for channel policy settings.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEmbeddable {
    
    @Column(name = "age_gate")
    private boolean ageGate;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "region_block")
    private List<String> regionBlocks;
}
