package com.youtube.edgecdncontrol.application.usecases;

import com.youtube.edgecdncontrol.application.dto.CdnRuleResponse;
import com.youtube.edgecdncontrol.application.dto.PageResponse;
import com.youtube.edgecdncontrol.application.mappers.CdnRuleMapper;
import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.repositories.CdnRuleRepository;
import com.youtube.edgecdncontrol.domain.valueobjects.FrontDoorProfileId;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCdnRulesUseCase {
    
    private final CdnRuleRepository ruleRepository;
    private final CdnRuleMapper mapper;
    
    public PageResponse<CdnRuleResponse> executeByProfile(
            String resourceGroup, 
            String profileName, 
            int page, 
            int size) {
        log.debug("Getting CDN rules for profile: {}/{}", resourceGroup, profileName);
        FrontDoorProfileId profileId = new FrontDoorProfileId(resourceGroup, profileName);
        List<CdnRule> rules = ruleRepository.findByFrontDoorProfile(profileId, page, size);
        
        List<CdnRuleResponse> content = rules.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        // Note: In production, implement proper pagination with total count
        return PageResponse.<CdnRuleResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(content.size())
                .totalPages((int) Math.ceil((double) content.size() / size))
                .build();
    }
    
    public PageResponse<CdnRuleResponse> executeByStatus(RuleStatus status, int page, int size) {
        log.debug("Getting CDN rules by status: {}", status);
        List<CdnRule> rules = ruleRepository.findByStatus(status, page, size);
        
        List<CdnRuleResponse> content = rules.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<CdnRuleResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(content.size())
                .totalPages((int) Math.ceil((double) content.size() / size))
                .build();
    }
}

