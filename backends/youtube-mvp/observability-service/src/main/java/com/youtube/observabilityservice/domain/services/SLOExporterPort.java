package com.youtube.observabilityservice.domain.services;

import com.youtube.observabilityservice.domain.entities.SLO;

/**
 * Port for exporting SLO metrics to external systems (Prometheus, Azure Monitor, etc.).
 */
public interface SLOExporterPort {
    /**
     * Exports SLO metrics.
     */
    void export(SLO slo);
}

