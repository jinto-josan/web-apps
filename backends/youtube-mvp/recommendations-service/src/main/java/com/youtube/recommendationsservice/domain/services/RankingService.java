package com.youtube.recommendationsservice.domain.services;

import com.youtube.recommendationsservice.domain.entities.RecommendedItem;
import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationContext;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;

import java.util.List;

public interface RankingService {
    List<RecommendedItem> rank(List<VideoCandidate> candidates, UserId userId, RecommendationContext context);
}

