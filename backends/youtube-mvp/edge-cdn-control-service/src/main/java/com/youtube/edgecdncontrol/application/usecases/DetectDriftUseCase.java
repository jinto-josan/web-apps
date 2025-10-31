package com.youtube.edgecdncontrol.application.usecases;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.repositories.CdnRuleRepository;
import com.youtube.edgecdncontrol.domain.services.DriftDetectionService;
import com.youtube.edgecdncontrol.domain.valueobjects.CdnRuleId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectDriftUseCase {
    
    private final CdnRuleRepository ruleRepository;
    private final DriftDetectionService driftDetectionService;
    
    @Transactional
    public List<DriftDetectionService.DriftFinding> execute(CdnRuleId ruleId) {
        log.info("Detecting drift for rule: {}", ruleId.getValue());
        
        CdnRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId.getValue()));
        
        List<DriftDetectionService.DriftFinding> findings = driftDetectionService.detectDrift(rule);
        
        if (!findings.isEmpty()) {
            log.warn("Drift detected for rule {}: {} findings", ruleId.getValue(), findings.size());
            // Mark rule as having drift detected
            CdnRule withDrift = rule.markDriftDetected();
            ruleRepository.save(withDrift);
        } else {
            log.info("No drift detected for rule: {}", ruleId.getValue());
        }
        
        return findings;
    }
}

