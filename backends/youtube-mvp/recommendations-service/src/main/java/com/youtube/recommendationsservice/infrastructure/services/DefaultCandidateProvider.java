package com.youtube.recommendationsservice.infrastructure.services;

import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import com.youtube.recommendationsservice.domain.repositories.VideoCandidateRepository;
import com.youtube.recommendationsservice.domain.services.CandidateProvider;
import com.youtube.recommendationsservice.domain.valueobjects.RecommendationContext;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class DefaultCandidateProvider implements CandidateProvider {
    
    private final VideoCandidateRepository videoCandidateRepository;
    
    @Override
    public List<VideoCandidate> getCandidates(UserId userId, RecommendationContext context, int count) {
        log.debug("Getting candidates from default provider for user: {}", userId.getValue());
        return videoCandidateRepository.findCandidatesForUser(userId, count);
    }
    
    @Override
    public String getProviderName() {
        return "default";
    }
}

