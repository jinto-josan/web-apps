package com.youtube.recommendationsservice.domain.repositories;

import com.youtube.recommendationsservice.domain.entities.UserFeatures;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;

import java.util.Optional;

public interface UserFeaturesRepository {
    Optional<UserFeatures> findByUserId(UserId userId);
    void save(UserFeatures userFeatures);
    boolean exists(UserId userId);
}

