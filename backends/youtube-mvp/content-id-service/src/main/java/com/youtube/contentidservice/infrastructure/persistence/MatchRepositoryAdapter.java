package com.youtube.contentidservice.infrastructure.persistence;

import com.youtube.contentidservice.domain.entities.Match;
import com.youtube.contentidservice.domain.repositories.MatchRepository;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.MatchScore;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import com.youtube.contentidservice.infrastructure.persistence.entity.MatchJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchRepositoryAdapter implements MatchRepository {
    private final JpaMatchRepository jpaRepository;

    @Override
    public void save(Match match) {
        jpaRepository.save(toEntity(match));
    }

    @Override
    public Optional<Match> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Match> findBySourceVideoId(VideoId videoId) {
        return jpaRepository.findBySourceVideoId(videoId.getValue()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Match> findByMatchedVideoId(VideoId videoId) {
        return jpaRepository.findByMatchedVideoId(videoId.getValue()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Match> findUnprocessed() {
        return jpaRepository.findByProcessedFalse().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByFingerprintIds(FingerprintId source, FingerprintId matched) {
        return jpaRepository.existsByFingerprintIds(source.getValue(), matched.getValue());
    }

    private MatchJpaEntity toEntity(Match match) {
        return MatchJpaEntity.builder()
                .id(match.getId())
                .sourceFingerprintId(match.getSourceFingerprintId().getValue())
                .matchedFingerprintId(match.getMatchedFingerprintId().getValue())
                .sourceVideoId(match.getSourceVideoId().getValue())
                .matchedVideoId(match.getMatchedVideoId().getValue())
                .score(match.getScore().getValue())
                .detectedAt(match.getDetectedAt())
                .processed(match.isProcessed())
                .build();
    }

    private Match toDomain(MatchJpaEntity entity) {
        return Match.builder()
                .id(entity.getId())
                .sourceFingerprintId(FingerprintId.of(entity.getSourceFingerprintId()))
                .matchedFingerprintId(FingerprintId.of(entity.getMatchedFingerprintId()))
                .sourceVideoId(VideoId.of(entity.getSourceVideoId()))
                .matchedVideoId(VideoId.of(entity.getMatchedVideoId()))
                .score(MatchScore.of(entity.getScore()))
                .detectedAt(entity.getDetectedAt())
                .processed(entity.getProcessed())
                .build();
    }
}

