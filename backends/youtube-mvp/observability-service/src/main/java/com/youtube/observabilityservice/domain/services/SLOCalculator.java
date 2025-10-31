package com.youtube.observabilityservice.domain.services;

import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.entities.SLI;

import java.util.List;

/**
 * Domain service for calculating SLO metrics from SLIs.
 */
public interface SLOCalculator {
    /**
     * Calculates the current SLO value from aggregated SLIs.
     */
    double calculateSLO(SLO slo, List<SLI> currentSLIs);

    /**
     * Calculates error budget burn rate.
     */
    double calculateErrorBudgetBurnRate(SLO slo, double currentSLO, double previousSLO);

    /**
     * Calculates remaining error budget percentage.
     */
    double calculateRemainingErrorBudget(SLO slo, double currentSLO);
}

