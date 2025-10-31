package com.youtube.contentidservice.application.services;

import com.youtube.contentidservice.application.commands.CreateFingerprintCommand;
import com.youtube.contentidservice.application.dto.FingerprintResponse;
import com.youtube.contentidservice.domain.entities.Fingerprint;
import com.youtube.contentidservice.domain.events.FingerprintCreatedEvent;
import com.youtube.contentidservice.domain.repositories.EventPublisher;
import com.youtube.contentidservice.domain.repositories.FingerprintRepository;
import com.youtube.contentidservice.domain.services.FingerprintEngine;
import com.youtube.contentidservice.domain.valueobjects.FingerprintData;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import com.youtube.contentidservice.application.mappers.ContentIdMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FingerprintService {
    private final FingerprintRepository fingerprintRepository;
    private final FingerprintEngine fingerprintEngine;
    private final EventPublisher eventPublisher;
    private final ContentIdMapper mapper;

    @Transactional
    public FingerprintResponse createFingerprint(CreateFingerprintCommand command) {
        log.info("Creating fingerprint for videoId: {}", command.getVideoId());

        VideoId videoId = VideoId.of(command.getVideoId());
        
        // Check if fingerprint already exists
        fingerprintRepository.findByVideoId(videoId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Fingerprint already exists for videoId: " + command.getVideoId());
                });

        // Generate fingerprint
        FingerprintData fingerprintData;
        if (command.getBlobUri() != null && !command.getBlobUri().isBlank()) {
            fingerprintData = fingerprintEngine.generateFingerprint(command.getBlobUri());
        } else {
            // In real implementation, fetch video stream from blob storage using videoId
            throw new UnsupportedOperationException("Video stream fetch not implemented. Provide blobUri.");
        }

        // Create fingerprint entity
        Fingerprint fingerprint = Fingerprint.create(videoId, fingerprintData);
        fingerprint.setId(FingerprintId.of(UUID.randomUUID()));
        
        // Save fingerprint
        fingerprintRepository.save(fingerprint);
        
        // Publish domain event (via outbox)
        eventPublisher.publish(new FingerprintCreatedEvent(
                fingerprint.getId(),
                videoId,
                fingerprintData.getBlobUri()
        ));

        log.info("Fingerprint created: {}", fingerprint.getId().getValue());
        return mapper.toResponse(fingerprint);
    }

    public FingerprintResponse getFingerprint(UUID fingerprintId) {
        Fingerprint fingerprint = fingerprintRepository.findById(FingerprintId.of(fingerprintId))
                .orElseThrow(() -> new IllegalArgumentException("Fingerprint not found: " + fingerprintId));
        return mapper.toResponse(fingerprint);
    }
}

