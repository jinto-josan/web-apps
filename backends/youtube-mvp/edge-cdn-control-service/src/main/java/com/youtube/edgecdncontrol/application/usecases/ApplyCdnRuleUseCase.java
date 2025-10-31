package com.youtube.edgecdncontrol.application.usecases;

import com.youtube.edgecdncontrol.application.dto.CdnRuleResponse;
import com.youtube.edgecdncontrol.application.mappers.CdnRuleMapper;
import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.repositories.CdnRuleRepository;
import com.youtube.edgecdncontrol.domain.services.AzureFrontDoorPort;
import com.youtube.edgecdncontrol.domain.valueobjects.CdnRuleId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplyCdnRuleUseCase {
    
    private final CdnRuleRepository ruleRepository;
    private final AzureFrontDoorPort azureFrontDoorPort;
    private final CdnRuleMapper mapper;
    
    @Transactional
    public CdnRuleResponse execute(CdnRuleId ruleId, boolean dryRun) {
        log.info("Applying CDN rule: {}, dryRun: {}", ruleId.getValue(), dryRun);
        
        CdnRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId.getValue()));
        
        if (dryRun) {
            log.info("Dry-run mode: validating rule without applying");
            // In dry-run, just validate without applying
            CdnRule validated = rule.markAsValidated();
            CdnRule saved = ruleRepository.save(validated);
            return mapper.toResponse(saved);
        }
        
        try {
            // Apply rule to Azure Front Door
            azureFrontDoorPort.applyRule(rule);
            
            // Mark as applied
            CdnRule applied = rule.markAsApplied();
            CdnRule saved = ruleRepository.save(applied);
            log.info("CDN rule applied successfully: {}", ruleId.getValue());
            return mapper.toResponse(saved);
        } catch (Exception e) {
            log.error("Failed to apply CDN rule: {}", ruleId.getValue(), e);
            CdnRule failed = rule.markAsFailed(e.getMessage());
            ruleRepository.save(failed);
            throw new RuntimeException("Failed to apply rule: " + e.getMessage(), e);
        }
    }
}

