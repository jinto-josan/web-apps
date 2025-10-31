package com.youtube.antiaabuseservice.domain.repositories;

import com.youtube.antiaabuseservice.domain.model.FeatureStore;

import java.util.Optional;

public interface FeatureStoreRepository {
    Optional<FeatureStore> findByUserIdAndFeatureSet(String userId, String featureSet);
    FeatureStore save(FeatureStore featureStore);
    void deleteByUserId(String userId);
}

