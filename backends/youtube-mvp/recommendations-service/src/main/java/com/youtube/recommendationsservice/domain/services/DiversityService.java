package com.youtube.recommendationsservice.domain.services;

import com.youtube.recommendationsservice.domain.entities.RecommendedItem;

import java.util.List;

public interface DiversityService {
    List<RecommendedItem> applyDiversityConstraints(List<RecommendedItem> items, int maxFromSameCategory);
}

