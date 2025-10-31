package com.youtube.edgecdncontrol.domain.services;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;
import com.youtube.edgecdncontrol.domain.valueobjects.FrontDoorProfileId;

import java.util.List;

/**
 * Port for interacting with Azure Front Door/CDN.
 * This is an outbound port (adapter interface) in hexagonal architecture.
 */
public interface AzureFrontDoorPort {
    
    /**
     * Applies a CDN rule to Azure Front Door.
     * 
     * @param rule The rule to apply
     * @throws RuntimeException if the application fails
     */
    void applyRule(CdnRule rule);
    
    /**
     * Purges cache for the specified paths.
     * 
     * @param profileId The Front Door profile
     * @param contentPaths The paths to purge
     * @param purgeType The type of purge operation
     * @throws RuntimeException if the purge fails
     */
    void purgeCache(FrontDoorProfileId profileId, List<String> contentPaths, PurgeRequest.PurgeType purgeType);
    
    /**
     * Gets the current configuration from Azure Front Door for comparison.
     * Used for drift detection.
     * 
     * @param rule The rule to compare
     * @return The current configuration from Azure (may be null if rule doesn't exist)
     */
    CdnRule getCurrentConfiguration(CdnRule rule);
}

