package com.youtube.contentidservice.application.services;

import com.youtube.contentidservice.application.commands.CreateMatchCommand;
import com.youtube.contentidservice.application.dto.MatchRequest;
import com.youtube.contentidservice.application.dto.MatchResponse;
import com.youtube.contentidservice.domain.entities.Match;
import com.youtube.contentidservice.domain.events.MatchDetectedEvent;
import com.youtube.contentidservice.domain.repositories.EventPublisher;
import com.youtube.contentidservice.domain.repositories.MatchRepository;
import com.youtube.contentidservice.domain.services.MatchEngine;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.MatchScore;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import com.youtube.contentidservice.application.mappers.ContentIdMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchEngine matchEngine;
    private final EventPublisher eventPublisher;
    private final ContentIdMapper mapper;

    @Transactional
    public List<MatchResponse> findMatches(MatchRequest request) {
        log.info("Finding matches for fingerprintId: {}", request.getFingerprintId());

        FingerprintId fingerprintId = FingerprintId.of(request.getFingerprintId());
        double threshold = request.getThreshold() > 0 ? request.getThreshold() : 0.7;

        List<MatchEngine.MatchResult> results = matchEngine.findMatches(fingerprintId, threshold);

        // Create match entities for new matches
        List<Match> matches = results.stream()
                .map(result -> {
                    // Check if match already exists (idempotency)
                    if (!matchRepository.existsByFingerprintIds(fingerprintId, result.matchedFingerprintId())) {
                        // In real implementation, fetch video IDs from fingerprints
                        // For now, we'll create matches without video IDs (they should be fetched)
                        Match match = Match.create(
                                fingerprintId,
                                result.matchedFingerprintId(),
                                VideoId.of(java.util.UUID.randomUUID()), // Should fetch from fingerprint
                                VideoId.of(java.util.UUID.randomUUID()), // Should fetch from fingerprint
                                result.score()
                        );
                        matchRepository.save(match);
                        
                        // Publish event via Event Hubs
                        eventPublisher.publish(new MatchDetectedEvent(
                                match.getId(),
                                match.getSourceFingerprintId(),
                                match.getMatchedFingerprintId(),
                                match.getSourceVideoId(),
                                match.getMatchedVideoId(),
                                match.getScore()
                        ));
                        
                        return match;
                    }
                    return null;
                })
                .filter(match -> match != null)
                .collect(Collectors.toList());

        return mapper.toMatchResponseList(matches);
    }

    @Transactional
    public MatchResponse createMatch(CreateMatchCommand command) {
        log.info("Creating match: source={}, matched={}", 
                command.getSourceFingerprintId(), command.getMatchedFingerprintId());

        FingerprintId source = FingerprintId.of(command.getSourceFingerprintId());
        FingerprintId matched = FingerprintId.of(command.getMatchedFingerprintId());

        // Check idempotency
        if (matchRepository.existsByFingerprintIds(source, matched)) {
            throw new IllegalArgumentException("Match already exists");
        }

        Match match = Match.create(
                source,
                matched,
                VideoId.of(command.getSourceVideoId()),
                VideoId.of(command.getMatchedVideoId()),
                MatchScore.of(command.getScore())
        );

        matchRepository.save(match);

        // Publish event
        eventPublisher.publish(new MatchDetectedEvent(
                match.getId(),
                match.getSourceFingerprintId(),
                match.getMatchedFingerprintId(),
                match.getSourceVideoId(),
                match.getMatchedVideoId(),
                match.getScore()
        ));

        return mapper.toResponse(match);
    }
}

