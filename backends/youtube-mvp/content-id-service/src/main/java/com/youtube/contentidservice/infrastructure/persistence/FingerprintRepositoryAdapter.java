package com.youtube.contentidservice.infrastructure.persistence;

import com.youtube.contentidservice.domain.entities.Fingerprint;
import com.youtube.contentidservice.domain.repositories.FingerprintRepository;
import com.youtube.contentidservice.domain.valueobjects.FingerprintData;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import com.youtube.contentidservice.infrastructure.persistence.entity.FingerprintJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FingerprintRepositoryAdapter implements FingerprintRepository {
    private final JpaFingerprintRepository jpaRepository;

    @Override
    public void save(Fingerprint fingerprint) {
        jpaRepository.save(toEntity(fingerprint));
    }

    @Override
    public Optional<Fingerprint> findById(FingerprintId id) {
        return jpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<Fingerprint> findByVideoId(VideoId videoId) {
        return jpaRepository.findByVideoId(videoId.getValue()).map(this::toDomain);
    }

    @Override
    public List<Fingerprint> findAllPending() {
        return jpaRepository.findByStatus("PENDING").stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private FingerprintJpaEntity toEntity(Fingerprint fingerprint) {
        return FingerprintJpaEntity.builder()
                .id(fingerprint.getId() != null ? fingerprint.getId().getValue() : null)
                .videoId(fingerprint.getVideoId().getValue())
                .blobUri(fingerprint.getData().getBlobUri())
                .algorithm(fingerprint.getData().getAlgorithm())
                .durationSeconds(fingerprint.getData().getDurationSeconds())
                .status(fingerprint.getStatus())
                .createdAt(fingerprint.getCreatedAt())
                .processedAt(fingerprint.getProcessedAt())
                .build();
    }

    private Fingerprint toDomain(FingerprintJpaEntity entity) {
        FingerprintData data = FingerprintData.of(
                new byte[0], // Hash stored in blob, not in DB
                entity.getAlgorithm(),
                entity.getDurationSeconds(),
                entity.getBlobUri()
        );
        
        return Fingerprint.builder()
                .id(FingerprintId.of(entity.getId()))
                .videoId(VideoId.of(entity.getVideoId()))
                .data(data)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .processedAt(entity.getProcessedAt())
                .build();
    }
}

