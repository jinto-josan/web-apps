package com.youtube.videotranscodeservice.application.services;

import com.youtube.videotranscodeservice.domain.entities.*;
import com.youtube.videotranscodeservice.domain.repositories.*;
import com.youtube.videotranscodeservice.domain.valueobjects.*;
import com.youtube.videotranscodeservice.infrastructure.external.AzureMediaServicesAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaProcessingOrchestrator {
    
    private final AzureMediaServicesAdapter amsAdapter;
    private final MediaProcessingRepository jobRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final DRMKeyRepository drmKeyRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    
    public MediaProcessingJob startProcessing(UUID videoId, UUID userId, String inputBlobUrl) {
        log.info("Starting media processing for video: {}", videoId);
        
        // Create job record
        MediaProcessingJob job = MediaProcessingJob.builder()
                .jobId(generateJobId())
                .videoId(videoId)
                .userId(userId)
                .status(ProcessingStatus.QUEUED)
                .createdAt(Instant.now())
                .build();
        
        jobRepository.save(job);
        
        try {
            // Step 1: Submit encoding job to Azure Media Services
            String transformId = getOrCreateTransform();
            String azureJobId = amsAdapter.submitEncodingJob(transformId, inputBlobUrl);
            
            job.setAzureJobId(azureJobId);
            job.setTransformId(transformId);
            job.updateStatus(ProcessingStatus.ENCODING);
            jobRepository.update(job);
            
            log.info("Encoding job submitted: {} for video: {}", azureJobId, videoId);
            
            // Process asynchronously
            processJobAsync(job);
            
            return job;
        } catch (Exception e) {
            log.error("Failed to start processing for video: {}", videoId, e);
            job.setError(e.getMessage());
            jobRepository.update(job);
            throw new RuntimeException("Failed to start media processing", e);
        }
    }
    
    private void processJobAsync(MediaProcessingJob job) {
        // Monitor and process job in background
        try {
            JobResult result = amsAdapter.waitForJobCompletion(job.getAzureJobId());
            
            if (result.getStatus() == JobStatus.FINISHED) {
                job.setAssetId(result.getAssetId());
                completeMediaProcessing(job);
            } else {
                job.setError(result.getErrorMessage());
                job.updateStatus(ProcessingStatus.FAILED);
                jobRepository.update(job);
                publishFailureEvent(job);
            }
        } catch (Exception e) {
            log.error("Error processing job: {}", job.getJobId(), e);
            job.setError(e.getMessage());
            job.updateStatus(ProcessingStatus.FAILED);
            jobRepository.update(job);
            publishFailureEvent(job);
        }
    }
    
    private void completeMediaProcessing(MediaProcessingJob job) {
        try {
            job.updateStatus(ProcessingStatus.THUMBNAIL_GENERATING);
            jobRepository.update(job);
            
            // Generate thumbnails
            List<String> thumbnailUrls = generateThumbnails(job);
            
            job.updateStatus(ProcessingStatus.PACKAGING);
            jobRepository.update(job);
            
            // Package for streaming (HLS/DASH)
            PackagingResult packagingResult = amsAdapter.packageHlsDash(job.getAssetId());
            
            job.updateStatus(ProcessingStatus.DRM_PROCESSING);
            jobRepository.update(job);
            
            // Apply DRM protection
            DRMConfiguration drmConfig = amsAdapter.applyDRMProtection(job.getAssetId());
            
            // Save DRM keys
            saveDRMKeys(job.getVideoId(), drmConfig);
            
            job.updateStatus(ProcessingStatus.COMPLETED);
            job.addMetadata("hlsUrl", packagingResult.getHlsUrl());
            job.addMetadata("dashUrl", packagingResult.getDashUrl());
            jobRepository.update(job);
            
            // Publish success events
            publishSuccessEvents(job, thumbnailUrls, packagingResult, drmConfig);
            
            log.info("Media processing completed successfully for video: {}", job.getVideoId());
        } catch (Exception e) {
            log.error("Error completing media processing for job: {}", job.getJobId(), e);
            job.setError(e.getMessage());
            job.updateStatus(ProcessingStatus.FAILED);
            jobRepository.update(job);
            publishFailureEvent(job);
        }
    }
    
    private List<String> generateThumbnails(MediaProcessingJob job) {
        List<String> timeCodes = List.of("00:00:03", "00:00:10", "00:00:20");
        List<String> thumbnailUrls = amsAdapter.generateThumbnails(job.getAssetId(), timeCodes);
        
        List<Thumbnail> thumbnails = thumbnailUrls.stream()
                .map(url -> Thumbnail.builder()
                        .thumbnailId(UUID.randomUUID())
                        .videoId(job.getVideoId())
                        .url(url)
                        .timeCode(timeCodes.get(thumbnailUrls.indexOf(url)))
                        .size(ThumbnailSize.MEDIUM)
                        .selected(false)
                        .createdAt(Instant.now())
                        .build())
                .collect(Collectors.toList());
        
        thumbnailRepository.saveAll(thumbnails);
        return thumbnailUrls;
    }
    
    private void saveDRMKeys(UUID videoId, DRMConfiguration drmConfig) {
        List<DRMKey> drmKeys = drmConfig.getContentKeys().entrySet().stream()
                .map(entry -> DRMKey.builder()
                        .contentKeyId(entry.getValue())
                        .videoId(videoId)
                        .drmType(DRMType.valueOf(entry.getKey().toUpperCase()))
                        .keyIdentifier(entry.getValue())
                        .createdAt(Instant.now())
                        .build())
                .collect(Collectors.toList());
        
        drmKeyRepository.saveAll(drmKeys);
    }
    
    private void publishSuccessEvents(MediaProcessingJob job, List<String> thumbnails, 
                                      PackagingResult packagingResult, DRMConfiguration drmConfig) {
        // Publish media processing completed event
        outboxEventPublisher.publishEvent(
            new MediaProcessingCompletedEvent(
                job.getVideoId(),
                job.getAssetId(),
                packagingResult.getHlsUrl(),
                packagingResult.getDashUrl(),
                thumbnails,
                drmConfig,
                job.getProcessingDurationMs()
            )
        );
        
        // Publish thumbnail generated event
        outboxEventPublisher.publishEvent(
            new ThumbnailGeneratedEvent(job.getVideoId(), thumbnails)
        );
    }
    
    private void publishFailureEvent(MediaProcessingJob job) {
        outboxEventPublisher.publishEvent(
            new MediaProcessingFailedEvent(job.getVideoId(), job.getErrorMessage())
        );
    }
    
    private String getOrCreateTransform() {
        // TODO: Implement transform creation/retrieval logic
        return "transform-" + UUID.randomUUID();
    }
    
    private String generateJobId() {
        return "job-" + UUID.randomUUID();
    }
    
    // Event classes
    public static class MediaProcessingCompletedEvent {
        public final UUID videoId;
        public final String assetId;
        public final String hlsUrl;
        public final String dashUrl;
        public final List<String> thumbnails;
        public final DRMConfiguration drmConfig;
        public final long processingTimeMs;
        
        public MediaProcessingCompletedEvent(UUID videoId, String assetId, String hlsUrl, 
                                           String dashUrl, List<String> thumbnails, 
                                           DRMConfiguration drmConfig, long processingTimeMs) {
            this.videoId = videoId;
            this.assetId = assetId;
            this.hlsUrl = hlsUrl;
            this.dashUrl = dashUrl;
            this.thumbnails = thumbnails;
            this.drmConfig = drmConfig;
            this.processingTimeMs = processingTimeMs;
        }
    }
    
    public static class ThumbnailGeneratedEvent {
        public final UUID videoId;
        public final List<String> thumbnails;
        
        public ThumbnailGeneratedEvent(UUID videoId, List<String> thumbnails) {
            this.videoId = videoId;
            this.thumbnails = thumbnails;
        }
    }
    
    public static class MediaProcessingFailedEvent {
        public final UUID videoId;
        public final String error;
        
        public MediaProcessingFailedEvent(UUID videoId, String error) {
            this.videoId = videoId;
            this.error = error;
        }
    }
    
    // OutboxEventPublisher interface
    public interface OutboxEventPublisher {
        void publishEvent(Object event);
    }
}

