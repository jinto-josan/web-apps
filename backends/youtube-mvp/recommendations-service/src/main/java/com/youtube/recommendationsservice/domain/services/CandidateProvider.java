package com.youtube.recommendationsservice.domain.services;

import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationContext;

import java.util.List;

public interface CandidateProvider {
    List<VideoCandidate> getCandidates(UserId userId, RecommendationContext context, int count);
    String getProviderName();
}

