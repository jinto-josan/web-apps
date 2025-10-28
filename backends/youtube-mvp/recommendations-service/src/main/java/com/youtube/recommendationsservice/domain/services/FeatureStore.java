package com.youtube.recommendationsservice.domain.services;

import com.youtube.recommendationsservice.domain.valueobjects.FeatureVector;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import com.youtube.recommendationsservice.domain.valueobjects.VideoId;

import java.util.Optional;

public interface FeatureStore {
    Optional<FeatureVector> getUserFeatures(UserId userId);
    Optional<FeatureVector> getVideoFeatures(VideoId videoId);
    void cacheUserFeatures(UserId userId, FeatureVector features);
    void cacheVideoFeatures(VideoId videoId, FeatureVector features);
}

