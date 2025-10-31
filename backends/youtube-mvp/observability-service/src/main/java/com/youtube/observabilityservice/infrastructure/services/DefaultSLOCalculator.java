package com.youtube.observabilityservice.infrastructure.services;

import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.entities.SLI;
import com.youtube.observabilityservice.domain.services.SLOCalculator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultSLOCalculator implements SLOCalculator {
    
    @Override
    public double calculateSLO(SLO slo, List<SLI> currentSLIs) {
        if (currentSLIs == null || currentSLIs.isEmpty()) {
            return 0.0;
        }
        
        // Simple average of SLI values (can be made more sophisticated)
        double sum = currentSLIs.stream()
                .filter(sli -> sli.getLastValue() != null)
                .mapToDouble(SLI::getLastValue)
                .sum();
        
        long count = currentSLIs.stream()
                .filter(sli -> sli.getLastValue() != null)
                .count();
        
        return count > 0 ? sum / count : 0.0;
    }
    
    @Override
    public double calculateErrorBudgetBurnRate(SLO slo, double currentSLO, double previousSLO) {
        if (previousSLO == 0.0) {
            return 0.0;
        }
        
        double burnRate = (previousSLO - currentSLO) / (100.0 - slo.getTargetPercent());
        return Math.max(0.0, burnRate);
    }
    
    @Override
    public double calculateRemainingErrorBudget(SLO slo, double currentSLO) {
        double errorBudgetTotal = 100.0 - slo.getTargetPercent();
        double used = slo.getTargetPercent() - currentSLO;
        return Math.max(0.0, errorBudgetTotal - used);
    }
}

