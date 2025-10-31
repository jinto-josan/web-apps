package com.youtube.edgecdncontrol.application.usecases;

import com.youtube.edgecdncontrol.application.dto.CreateCdnRuleRequest;
import com.youtube.edgecdncontrol.application.dto.CdnRuleResponse;
import com.youtube.edgecdncontrol.application.mappers.CdnRuleMapper;
import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.repositories.CdnRuleRepository;
import com.youtube.edgecdncontrol.domain.services.RuleValidationService;
import com.youtube.edgecdncontrol.domain.valueobjects.CdnRuleId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCdnRuleUseCase {
    
    private final CdnRuleRepository ruleRepository;
    private final CdnRuleMapper mapper;
    private final RuleValidationService validationService;
    
    @Transactional
    public CdnRuleResponse execute(CreateCdnRuleRequest request, String createdBy) {
        log.info("Creating CDN rule: {}", request.getName());
        
        // Map to domain entity
        CdnRule rule = mapper.toDomain(request);
        
        // Set ID and metadata
        CdnRule ruleWithId = CdnRule.builder()
                .id(CdnRuleId.generate())
                .name(rule.getName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .status(rule.getStatus())
                .frontDoorProfile(mapper.toFrontDoorProfileId(request.getResourceGroup(), request.getFrontDoorProfileName()))
                .priority(rule.getPriority())
                .matchConditions(rule.getMatchConditions())
                .action(rule.getAction())
                .metadata(rule.getMetadata())
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version(UUID.randomUUID().toString())
                .rollbackFromRuleId(rule.getRollbackFromRuleId())
                .build();
        
        // Validate
        RuleValidationService.ValidationResult validation = validationService.validate(ruleWithId);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Validation failed: " + String.join(", ", validation.errors()));
        }
        
        // Save
        CdnRule saved = ruleRepository.save(ruleWithId);
        log.info("CDN rule created successfully with ID: {}", saved.getId().getValue());
        
        return mapper.toResponse(saved);
    }
}

