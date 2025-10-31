package com.youtube.experimentationservice.infrastructure.adapters.cosmos;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ExperimentCosmosRepository extends CrudRepository<ExperimentCosmosEntity, String> {
    Optional<ExperimentCosmosEntity> findByKey(String key);
}

