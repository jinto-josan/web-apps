package com.youtube.edgecdncontrol.infrastructure.adapters.persistence.jpa;

import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity.CdnRuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdnRuleJpaRepository extends JpaRepository<CdnRuleEntity, String> {
    Page<CdnRuleEntity> findByResourceGroupAndProfileName(String resourceGroup, String profileName, Pageable pageable);
    Page<CdnRuleEntity> findByStatus(RuleStatus status, Pageable pageable);
}

