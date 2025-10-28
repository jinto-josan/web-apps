package com.youtube.recommendationsservice.application.usecases;

import com.youtube.recommendationsservice.application.dto.RecommendationRequest;
import com.youtube.recommendationsservice.application.dto.RecommendationResponse;
import com.youtube.recommendationsservice.domain.entities.RecommendedItem;
import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import com.youtube.recommendationsservice.domain.services.CandidateProvider;
import com.youtube.recommendationsservice.domain.services.DiversityService;
import com.youtube.recommendationsservice.domain.services.RankingService;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationContext;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetHomeRecommendationsUseCase {
    
    private final List<CandidateProvider> candidateProviders;
    private final RankingService rankingService;
    private final DiversityService diversityService;
    
    public RecommendationResponse execute(RecommendationRequest request) {
        log.info("Processing home recommendations for userId: {}", request.getUserId());
        
        UserId userId = UserId.from(request.getUserId());
        RecommendationContext context = buildContext(request);
        
        // Stage 1: Candidate generation
        List<VideoCandidate> candidates = collectCandidates(userId, context, request.getLimit() * 2);
        log.debug("Collected {} candidates", candidates.size());
        
        if (candidates.isEmpty()) {
            return buildEmptyResponse(request.getUserId(), "home");
        }
        
        // Stage 2: Ranking
        List<RecommendedItem> rankedItems = rankingService.rank(candidates, userId, context);
        
        // Stage 3: Diversity
        List<RecommendedItem> diversifiedItems = diversityService.applyDiversityConstraints(
            rankedItems, 
            request.getLimit() / 3 // Max 1/3 from same category
        );
        
        // Stage 4: Final selection
        List<RecommendedItem> finalRecommendations = diversifiedItems.stream()
            .limit(request.getLimit())
            .collect(Collectors.toList());
        
        return buildResponse(finalRecommendations, request, candidates.size());
    }
    
    private List<VideoCandidate> collectCandidates(UserId userId, RecommendationContext context, int count) {
        List<VideoCandidate> allCandidates = new ArrayList<>();
        
        for (CandidateProvider provider : candidateProviders) {
            try {
                List<VideoCandidate> candidates = provider.getCandidates(userId, context, count);
                allCandidates.addAll(candidates);
                log.debug("Provider {} returned {} candidates", provider.getProviderName(), candidates.size());
            } catch (Exception e) {
                log.error("Error getting candidates from provider: {}", provider.getProviderName(), e);
                // Continue with other providers
            }
        }
        
        // Deduplicate by videoId
        return allCandidates.stream()
            .collect(Collectors.toMap(
                VideoCandidate::getVideoId,
                candidate -> candidate,
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .collect(Collectors.toList());
    }
    
    private RecommendationContext buildContext(RecommendationRequest request) {
        return RecommendationContext.builder()
            .device(request.getDevice())
            .location(request.getLocation())
            .language(request.getLanguage())
            .abTestVariant(request.getAbTestVariant())
            .build();
    }
    
    private RecommendationResponse buildResponse(List<RecommendedItem> recommendations, 
                                                RecommendationRequest request, 
                                                int totalCandidates) {
        return RecommendationResponse.builder()
            .recommendations(recommendations.stream()
                .map(rec -> RecommendationResponse.RecommendedVideoDto.builder()
                    .videoId(rec.getVideoId().getValue())
                    .score(rec.getScore().getValue())
                    .reason(rec.getReason())
                    .recommendedAt(rec.getRecommendedAt())
                    .additionalInfo(rec.getMetadata())
                    .build())
                .collect(Collectors.toList()))
            .metadata(RecommendationResponse.Metadata.builder()
                .userId(request.getUserId())
                .requestType("home")
                .timestamp(java.time.Instant.now())
                .abTestVariant(request.getAbTestVariant())
                .totalCandidates(totalCandidates)
                .totalReturned(recommendations.size())
                .build())
            .build();
    }
    
    private RecommendationResponse buildEmptyResponse(String userId, String requestType) {
        return RecommendationResponse.builder()
            .recommendations(List.of())
            .metadata(RecommendationResponse.Metadata.builder()
                .userId(userId)
                .requestType(requestType)
                .timestamp(java.time.Instant.now())
                .totalCandidates(0)
                .totalReturned(0)
                .build())
            .build();
    }
}

