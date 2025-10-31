package com.youtube.experimentationservice.domain.repositories;

import com.youtube.experimentationservice.domain.model.UserCohort;

import java.util.Optional;

public interface CohortRepository {
    Optional<UserCohort> findByUserIdAndExperimentKey(String userId, String experimentKey);
    UserCohort save(UserCohort cohort);
    void deleteByUserIdAndExperimentKey(String userId, String experimentKey);
}

