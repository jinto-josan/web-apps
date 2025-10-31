package com.youtube.observabilityservice.infrastructure.services;

import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.services.SLOExporterPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrometheusSLOExporter implements SLOExporterPort {
    
    private final MeterRegistry meterRegistry;
    
    @Override
    public void export(SLO slo) {
        Tags tags = Tags.of(
                "slo_id", slo.getId().getValue().toString(),
                "slo_name", slo.getName(),
                "service_name", slo.getServiceName()
        );
        
        if (slo.getLabels() != null && !slo.getLabels().isEmpty()) {
            Tags.Builder builder = Tags.builder();
            slo.getLabels().forEach((key, value) -> builder.and(key, value));
            tags = builder.and(tags).build();
        }
        
        // Export SLO target
        meterRegistry.gauge("slo_target_percent", tags, slo.getTargetPercent());
        
        // Export current SLO value (if available)
        if (slo.getErrorBudgetRemaining() != null) {
            meterRegistry.gauge("slo_current_percent", tags, 
                    100.0 - slo.getErrorBudget() + slo.getErrorBudgetRemaining());
        }
        
        // Export error budget
        if (slo.getErrorBudget() != null) {
            meterRegistry.gauge("slo_error_budget_total", tags, slo.getErrorBudget());
        }
        
        if (slo.getErrorBudgetRemaining() != null) {
            meterRegistry.gauge("slo_error_budget_remaining", tags, slo.getErrorBudgetRemaining());
        }
        
        log.debug("Exported SLO metrics for: {}", slo.getName());
    }
}

