package com.youtube.edgecdncontrol.infrastructure.services;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.services.AzureFrontDoorPort;
import com.youtube.edgecdncontrol.domain.services.DriftDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriftDetectionServiceImpl implements DriftDetectionService {
    
    private final AzureFrontDoorPort azureFrontDoorPort;
    
    @Override
    public List<DriftFinding> detectDrift(CdnRule rule) {
        log.info("Detecting drift for rule {}", rule.getId().getValue());
        List<DriftFinding> findings = new ArrayList<>();
        
        try {
            CdnRule currentConfig = azureFrontDoorPort.getCurrentConfiguration(rule);
            if (currentConfig == null) {
                findings.add(new DriftFinding(
                        "existence",
                        "Rule should exist",
                        "Rule not found in Azure",
                        "CRITICAL"
                ));
                return findings;
            }
            
            // Compare configurations
            if (!currentConfig.getName().equals(rule.getName())) {
                findings.add(new DriftFinding(
                        "name",
                        rule.getName(),
                        currentConfig.getName(),
                        "WARNING"
                ));
            }
            
            // Compare match conditions
            if (!currentConfig.getMatchConditions().equals(rule.getMatchConditions())) {
                findings.add(new DriftFinding(
                        "matchConditions",
                        rule.getMatchConditions().toString(),
                        currentConfig.getMatchConditions().toString(),
                        "CRITICAL"
                ));
            }
            
            // Compare actions
            if (!currentConfig.getAction().equals(rule.getAction())) {
                findings.add(new DriftFinding(
                        "action",
                        rule.getAction().toString(),
                        currentConfig.getAction().toString(),
                        "CRITICAL"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error detecting drift for rule {}: {}", rule.getId().getValue(), e.getMessage(), e);
            findings.add(new DriftFinding(
                    "error",
                    "No error",
                    e.getMessage(),
                    "CRITICAL"
            ));
        }
        
        return findings;
    }
}

