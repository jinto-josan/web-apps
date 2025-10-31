package com.youtube.experimentationservice.infrastructure.adapters.cosmos;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CohortCosmosRepository extends CrudRepository<CohortCosmosEntity, String> {
    Optional<CohortCosmosEntity> findByUserIdAndExperimentKey(String userId, String experimentKey);
}

