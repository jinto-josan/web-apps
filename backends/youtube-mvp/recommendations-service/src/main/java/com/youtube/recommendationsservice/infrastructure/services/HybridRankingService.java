package com.youtube.recommendationsservice.infrastructure.services;

import com.youtube.recommendationsservice.domain.entities.RecommendedItem;
import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import com.youtube.recommendationsservice.domain.services.RankingService;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationContext;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationScore;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridRankingService implements RankingService {
    
    @Override
    public List<RecommendedItem> rank(List<VideoCandidate> candidates, UserId userId, RecommendationContext context) {
        log.debug("Ranking {} candidates for user: {}", candidates.size(), userId.getValue());
        
        return candidates.stream()
            .map(candidate -> {
                double score = calculateHybridScore(candidate, userId, context);
                return RecommendedItem.builder()
                    .videoId(candidate.getVideoId())
                    .score(RecommendationScore.of(score))
                    .reason(generateReason(candidate, score))
                    .recommendedAt(java.time.Instant.now())
                    .metadata(Map.of(
                        "category", candidate.getCategory(),
                        "title", candidate.getTitle()
                    ))
                    .build();
            })
            .sorted((a, b) -> b.getScore().getValue().compareTo(a.getScore().getValue()))
            .collect(Collectors.toList());
    }
    
    private double calculateHybridScore(VideoCandidate candidate, UserId userId, RecommendationContext context) {
        // Multi-factor scoring:
        // 1. Recency (30%)
        // 2. Relevance (40%)
        // 3. Popularity (20%)
        // 4. Diversity (10%)
        
        double recencyScore = calculateRecencyScore(candidate);
        double relevanceScore = calculateRelevanceScore(candidate, userId, context);
        double popularityScore = calculatePopularityScore(candidate);
        double diversityScore = calculateDiversityScore(candidate);
        
        double finalScore = (recencyScore * 0.3) + 
                           (relevanceScore * 0.4) + 
                           (popularityScore * 0.2) + 
                           (diversityScore * 0.1);
        
        // Ensure score is between 0 and 1
        return Math.min(Math.max(finalScore, 0.0), 1.0);
    }
    
    private double calculateRecencyScore(VideoCandidate candidate) {
        long daysOld = ChronoUnit.DAYS.between(candidate.getPublishedAt(), java.time.Instant.now());
        return Math.exp(-daysOld / 30.0); // Decay factor
    }
    
    private double calculateRelevanceScore(VideoCandidate candidate, UserId userId, RecommendationContext context) {
        // Mock relevance based on category and context
        double score = 0.5; // Base score
        
        // Add context-specific relevance
        if (context.getLanguage() != null && candidate.getMetadata().containsKey("language")) {
            String videoLanguage = (String) candidate.getMetadata().get("language");
            if (context.getLanguage().equals(videoLanguage)) {
                score += 0.2;
            }
        }
        
        return Math.min(score, 1.0);
    }
    
    private double calculatePopularityScore(VideoCandidate candidate) {
        // Mock popularity based on metadata
        Object views = candidate.getMetadata().get("views");
        if (views instanceof Number) {
            return Math.min(((Number) views).doubleValue() / 1000000.0, 1.0);
        }
        return 0.5;
    }
    
    private double calculateDiversityScore(VideoCandidate candidate) {
        // Encourage diversity in recommendations
        return 0.5;
    }
    
    private String generateReason(VideoCandidate candidate, double score) {
        if (score > 0.8) {
            return "Highly relevant based on your interests";
        } else if (score > 0.6) {
            return "Trending in your area";
        } else {
            return "Similar content you might enjoy";
        }
    }
}

