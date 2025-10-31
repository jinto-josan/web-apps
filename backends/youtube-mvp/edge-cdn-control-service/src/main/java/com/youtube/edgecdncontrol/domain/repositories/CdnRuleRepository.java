package com.youtube.edgecdncontrol.domain.repositories;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.valueobjects.CdnRuleId;
import com.youtube.edgecdncontrol.domain.valueobjects.FrontDoorProfileId;

import java.util.List;
import java.util.Optional;

public interface CdnRuleRepository {
    CdnRule save(CdnRule rule);
    Optional<CdnRule> findById(CdnRuleId id);
    List<CdnRule> findByFrontDoorProfile(FrontDoorProfileId profileId, int page, int size);
    List<CdnRule> findByStatus(com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus status, int page, int size);
    void delete(CdnRuleId id);
    boolean existsById(CdnRuleId id);
}

