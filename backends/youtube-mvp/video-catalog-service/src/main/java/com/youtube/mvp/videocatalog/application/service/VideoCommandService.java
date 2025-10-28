package com.youtube.mvp.videocatalog.application.service;

import com.youtube.mvp.videocatalog.application.dto.*;
import com.youtube.mvp.videocatalog.application.mapper.VideoMapper;
import com.youtube.mvp.videocatalog.domain.event.VideoPublishedEvent;
import com.youtube.mvp.videocatalog.domain.model.*;
import com.youtube.mvp.videocatalog.domain.repository.VideoRepository;
import com.youtube.mvp.videocatalog.domain.service.VideoDomainService;
import com.youtube.mvp.videocatalog.infrastructure.messaging.VideoEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Command service for video operations (CQRS write side).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoCommandService {
    
    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;
    private final VideoDomainService domainService;
    private final VideoEventPublisher eventPublisher;
    
    /**
     * Creates a new video.
     */
    @Transactional
    public VideoResponse createVideo(CreateVideoRequest request) {
        log.info("Creating video for channel: {}", request.getChannelId());
        
        // Generate ID
        String videoId = generateVideoId();
        
        // Build domain entity
        Video video = Video.builder()
                .videoId(videoId)
                .title(request.getTitle())
                .description(request.getDescription())
                .channelId(request.getChannelId())
                .ownerId(request.getOwnerId())
                .category(request.getCategory())
                .language(request.getLanguage())
                .visibility(parseVisibility(request.getVisibility()))
                .tags(request.getTags())
                .state(VideoState.DRAFT)
                .version(domainService.generateVersion())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        // Add localized content
        if (request.getLocalizedTitles() != null) {
            request.getLocalizedTitles().forEach(lt -> {
                LocalizedText localized = LocalizedText.builder()
                        .language(lt.getLanguage())
                        .text(lt.getText())
                        .build();
                video.addLocalizedTitle(localized);
            });
        }
        
        if (request.getLocalizedDescriptions() != null) {
            request.getLocalizedDescriptions().forEach(ld -> {
                LocalizedText localized = LocalizedText.builder()
                        .language(ld.getLanguage())
                        .text(ld.getText())
                        .build();
                video.addLocalizedDescription(localized);
            });
        }
        
        // Save
        Video saved = videoRepository.save(video);
        log.info("Video created: {}", saved.getVideoId());
        
        return videoMapper.toResponse(saved);
    }
    
    /**
     * Updates a video.
     */
    @Transactional
    public VideoResponse updateVideo(String videoId, UpdateVideoRequest request, String ifMatch) {
        log.info("Updating video: {}", videoId);
        
        Video video = findVideoOrThrow(videoId);
        
        // ETag validation
        if (ifMatch != null && !ifMatch.equals(video.getVersion())) {
            throw new IllegalStateException("Version mismatch. Expected: " + video.getVersion());
        }
        
        // Update fields
        if (request.getTitle() != null) video.updateMetadata(request.getTitle(), null, null, null);
        if (request.getDescription() != null) video.updateMetadata(null, request.getDescription(), null, null);
        if (request.getTags() != null) video.updateMetadata(null, null, request.getTags(), null);
        if (request.getCategory() != null) video.updateMetadata(null, null, null, request.getCategory());
        
        // Update localized content
        if (request.getLocalizedTitles() != null) {
            request.getLocalizedTitles().forEach(lt -> video.addLocalizedTitle(
                    LocalizedText.builder()
                            .language(lt.getLanguage())
                            .text(lt.getText())
                            .build()
            ));
        }
        
        if (request.getLocalizedDescriptions() != null) {
            request.getLocalizedDescriptions().forEach(ld -> video.addLocalizedDescription(
                    LocalizedText.builder()
                            .language(ld.getLanguage())
                            .text(ld.getText())
                            .build()
            ));
        }
        
        // Update version
        video = video.toBuilder()
                .version(domainService.generateVersion())
                .updatedAt(Instant.now())
                .build();
        
        Video saved = videoRepository.save(video);
        log.info("Video updated: {}", saved.getVideoId());
        
        return videoMapper.toResponse(saved);
    }
    
    /**
     * Publishes a video.
     */
    @Transactional
    public VideoResponse publishVideo(String videoId) {
        log.info("Publishing video: {}", videoId);
        
        Video video = findVideoOrThrow(videoId);
        
        // Validate
        domainService.validatePublish(video);
        
        // Publish
        video.publish();
        
        // Update version
        video = video.toBuilder()
                .version(domainService.generateVersion())
                .build();
        
        Video saved = videoRepository.save(video);
        
        // Publish event
        VideoPublishedEvent event = VideoPublishedEvent.builder()
                .videoId(saved.getVideoId())
                .channelId(saved.getChannelId())
                .ownerId(saved.getOwnerId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .category(saved.getCategory())
                .visibility(saved.getVisibility().name())
                .publishedAt(saved.getPublishedAt())
                .occurredAt(Instant.now())
                .build();
        
        eventPublisher.publishVideoPublishedEvent(event);
        
        log.info("Video published: {}", saved.getVideoId());
        
        return videoMapper.toResponse(saved);
    }
    
    /**
     * Deletes a video.
     */
    @Transactional
    public void deleteVideo(String videoId) {
        log.info("Deleting video: {}", videoId);
        
        if (!videoRepository.existsById(videoId)) {
            throw new IllegalArgumentException("Video not found: " + videoId);
        }
        
        videoRepository.deleteById(videoId);
        log.info("Video deleted: {}", videoId);
    }
    
    private Video findVideoOrThrow(String videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + videoId));
    }
    
    private VideoVisibility parseVisibility(String visibility) {
        if (visibility == null) {
            return VideoVisibility.PRIVATE;
        }
        try {
            return VideoVisibility.valueOf(visibility.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid visibility: {}, defaulting to PRIVATE", visibility);
            return VideoVisibility.PRIVATE;
        }
    }
    
    private String generateVideoId() {
        return "video-" + UUID.randomUUID().toString();
    }
}

