package com.youtube.recommendationsservice.application.usecases;

import com.youtube.recommendationsservice.application.dto.RecommendationRequest;
import com.youtube.recommendationsservice.application.dto.RecommendationResponse;
import com.youtube.recommendationsservice.domain.entities.RecommendedItem;
import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import com.youtube.recommendationsservice.domain.repositories.VideoCandidateRepository;
import com.youtube.recommendationsservice.domain.services.RankingService;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationContext;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import com.youtube.recommendationsservice.domain.valueobjects.VideoId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetNextUpRecommendationsUseCase {
    
    private final VideoCandidateRepository candidateRepository;
    private final RankingService rankingService;
    
    public RecommendationResponse execute(RecommendationRequest request) {
        log.info("Processing next-up recommendations for userId: {}, videoId: {}", 
            request.getUserId(), request.getVideoId());
        
        if (request.getVideoId() == null || request.getVideoId().isBlank()) {
            throw new IllegalArgumentException("videoId is required for next-up recommendations");
        }
        
        UserId userId = UserId.from(request.getUserId());
        VideoId videoId = VideoId.from(request.getVideoId());
        RecommendationContext context = buildContext(request);
        
        // Get candidates related to the current video
        List<VideoCandidate> candidates = candidateRepository.findCandidatesForVideo(
            videoId, 
            request.getLimit() * 2
        );
        
        if (candidates.isEmpty()) {
            return buildEmptyResponse(request.getUserId(), "next-up");
        }
        
        // Rank candidates
        List<RecommendedItem> rankedItems = rankingService.rank(candidates, userId, context);
        
        // Return top N
        List<RecommendedItem> finalRecommendations = rankedItems.stream()
            .limit(request.getLimit())
            .collect(Collectors.toList());
        
        return buildResponse(finalRecommendations, request, candidates.size());
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
                .requestType("next-up")
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

