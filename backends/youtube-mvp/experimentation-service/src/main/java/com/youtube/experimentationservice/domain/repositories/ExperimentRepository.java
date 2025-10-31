package com.youtube.experimentationservice.domain.repositories;

import com.youtube.experimentationservice.domain.model.Experiment;

import java.util.List;
import java.util.Optional;

public interface ExperimentRepository {
    Optional<Experiment> findById(String id);
    Optional<Experiment> findByKey(String key);
    List<Experiment> findAllActive();
    Experiment save(Experiment experiment);
    void deleteById(String id);
}

