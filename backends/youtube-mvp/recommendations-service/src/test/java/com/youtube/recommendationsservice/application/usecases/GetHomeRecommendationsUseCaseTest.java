package com.youtube.recommendationsservice.application.usecases;

import com.youtube.recommendationsservice.application.dto.RecommendationRequest;
import com.youtube.recommendationsservice.application.dto.RecommendationResponse;
import com.youtube.recommendationsservice.domain.services.CandidateProvider;
import com.youtube.recommendationsservice.domain.services.DiversityService;
import com.youtube.recommendationsservice.domain.services.RankingService;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationContext;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetHomeRecommendationsUseCaseTest {
    
    @Mock
    private List<CandidateProvider> candidateProviders;
    
    @Mock
    private RankingService rankingService;
    
    @Mock
    private DiversityService diversityService;
    
    @InjectMocks
    private GetHomeRecommendationsUseCase useCase;
    
    @BeforeEach
    void setUp() {
        useCase = new GetHomeRecommendationsUseCase(
            List.of(),  // Will be mocked
            rankingService,
            diversityService
        );
    }
    
    @Test
    void execute_ShouldReturnRecommendations() {
        // Given
        RecommendationRequest request = RecommendationRequest.builder()
            .userId("user123")
            .limit(10)
            .abTestVariant("treatment")
            .build();
        
        // When
        RecommendationResponse response = useCase.execute(request);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getRecommendations());
        assertEquals("user123", response.getMetadata().getUserId());
    }
    
    @Test
    void execute_ShouldReturnEmptyResponseWhenNoCandidates() {
        // Given
        RecommendationRequest request = RecommendationRequest.builder()
            .userId("user123")
            .limit(10)
            .build();
        
        // When
        RecommendationResponse response = useCase.execute(request);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getRecommendations().isEmpty());
    }
}

