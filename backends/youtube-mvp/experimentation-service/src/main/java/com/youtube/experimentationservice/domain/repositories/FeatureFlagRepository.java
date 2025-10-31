package com.youtube.experimentationservice.domain.repositories;

import com.youtube.experimentationservice.domain.model.FeatureFlag;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepository {
    Optional<FeatureFlag> findByKey(String key);
    List<FeatureFlag> findAll();
    FeatureFlag save(FeatureFlag flag);
    void deleteByKey(String key);
}

