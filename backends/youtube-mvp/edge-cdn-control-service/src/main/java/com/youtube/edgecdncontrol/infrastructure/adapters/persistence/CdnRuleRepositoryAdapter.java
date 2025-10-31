package com.youtube.edgecdncontrol.infrastructure.adapters.persistence;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.repositories.CdnRuleRepository;
import com.youtube.edgecdncontrol.domain.valueobjects.CdnRuleId;
import com.youtube.edgecdncontrol.domain.valueobjects.FrontDoorProfileId;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity.CdnRuleEntity;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.jpa.CdnRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CdnRuleRepositoryAdapter implements CdnRuleRepository {
    
    private final CdnRuleJpaRepository jpaRepository;
    private final CdnRuleEntityMapper mapper;
    
    @Override
    public CdnRule save(CdnRule rule) {
        CdnRuleEntity entity = mapper.toEntity(rule);
        CdnRuleEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<CdnRule> findById(CdnRuleId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public List<CdnRule> findByFrontDoorProfile(FrontDoorProfileId profileId, int page, int size) {
        return jpaRepository.findByResourceGroupAndProfileName(
                profileId.getResourceGroup(),
                profileId.getProfileName(),
                PageRequest.of(page, size))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CdnRule> findByStatus(RuleStatus status, int page, int size) {
        return jpaRepository.findByStatus(status, PageRequest.of(page, size))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(CdnRuleId id) {
        jpaRepository.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsById(CdnRuleId id) {
        return jpaRepository.existsById(id.getValue());
    }
}

