package com.youtube.antiaabuseservice.infrastructure.adapters.cosmos;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeatureStoreCosmosRepository extends CrudRepository<FeatureStoreCosmosEntity, String> {
    Optional<FeatureStoreCosmosEntity> findByUserIdAndFeatureSet(String userId, String featureSet);
}

