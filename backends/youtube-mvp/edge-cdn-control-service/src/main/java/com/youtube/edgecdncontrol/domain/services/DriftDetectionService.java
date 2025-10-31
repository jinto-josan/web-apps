package com.youtube.edgecdncontrol.domain.services;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;

import java.util.List;

public interface DriftDetectionService {
    /**
     * Detects configuration drift by comparing the expected rule configuration
     * with the actual configuration in Azure Front Door/CDN.
     * 
     * @param rule The expected rule configuration
     * @return List of drift findings (empty if no drift detected)
     */
    List<DriftFinding> detectDrift(CdnRule rule);
    
    record DriftFinding(
        String property,
        Object expectedValue,
        Object actualValue,
        String severity // CRITICAL, WARNING, INFO
    ) {
    }
}

