package com.youtube.observabilityservice.domain.services;

import com.youtube.observabilityservice.domain.entities.SLI;

/**
 * Port for querying Azure Monitor / Log Analytics.
 */
public interface AzureMonitorQueryPort {
    /**
     * Executes a KQL query and returns a numeric result.
     */
    Double executeQuery(String kqlQuery);

    /**
     * Calculates SLI value from query.
     */
    Double calculateSLI(SLI sli);
}

