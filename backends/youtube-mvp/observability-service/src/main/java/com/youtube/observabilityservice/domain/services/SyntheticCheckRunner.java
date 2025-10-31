package com.youtube.observabilityservice.domain.services;

import com.youtube.observabilityservice.domain.entities.SyntheticCheck;
import com.youtube.observabilityservice.domain.entities.SyntheticCheckResult;

/**
 * Domain service for executing synthetic checks.
 */
public interface SyntheticCheckRunner {
    /**
     * Executes a synthetic check and returns the result.
     */
    SyntheticCheckResult run(SyntheticCheck check);
}

