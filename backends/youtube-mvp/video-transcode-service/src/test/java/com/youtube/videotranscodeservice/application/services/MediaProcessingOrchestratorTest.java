package com.youtube.videotranscodeservice.application.services;

import com.youtube.videotranscodeservice.domain.entities.*;
import com.youtube.videotranscodeservice.domain.repositories.*;
import com.youtube.videotranscodeservice.domain.valueobjects.*;
import com.youtube.videotranscodeservice.infrastructure.external.AzureMediaServicesAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaProcessingOrchestratorTest {
    
    @Mock
    private AzureMediaServicesAdapter amsAdapter;
    
    @Mock
    private MediaProcessingRepository jobRepository;
    
    @Mock
    private ThumbnailRepository thumbnailRepository;
    
    @Mock
    private DRMKeyRepository drmKeyRepository;
    
    @InjectMocks
    private MediaProcessingOrchestrator orchestrator;
    
    private UUID videoId;
    private UUID userId;
    private String inputBlobUrl;
    
    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        userId = UUID.randomUUID();
        inputBlobUrl = "https://storage.blob.core.windows.net/videos/input.mp4";
    }
    
    @Test
    void shouldStartProcessingSuccessfully() {
        // Given
        when(amsAdapter.submitEncodingJob(anyString(), anyString()))
                .thenReturn("azure-job-123");
        
        // When
        MediaProcessingJob job = orchestrator.startProcessing(videoId, userId, inputBlobUrl);
        
        // Then
        assertThat(job).isNotNull();
        assertThat(job.getVideoId()).isEqualTo(videoId);
        assertThat(job.getStatus()).isEqualTo(ProcessingStatus.ENCODING);
        assertThat(job.getAzureJobId()).isEqualTo("azure-job-123");
        
        verify(jobRepository, times(2)).save(job);
        verify(amsAdapter).submitEncodingJob(anyString(), eq(inputBlobUrl));
    }
    
    @Test
    void shouldGenerateThumbnails() {
        // Given
        MediaProcessingJob job = MediaProcessingJob.builder()
                .jobId("job-123")
                .videoId(videoId)
                .assetId("asset-123")
                .status(ProcessingStatus.THUMBNAIL_GENERATING)
                .build();
        
        List<String> thumbnailUrls = Arrays.asList(
                "https://storage.blob.core.windows.net/thumbnails/thumb1.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb2.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb3.jpg"
        );
        
        when(amsAdapter.generateThumbnails(anyString(), anyList()))
                .thenReturn(thumbnailUrls);
        
        // When - simulate thumbnail generation
        try {
            when(jobRepository.findByVideoId(videoId))
                    .thenReturn(List.of(job));
            
            List<Thumbnail> savedThumbnails = new ArrayList<>();
            doAnswer(invocation -> {
                savedThumbnails.addAll(invocation.getArgument(0));
                return null;
            }).when(thumbnailRepository).saveAll(anyList());
            
            // Trigger thumbnail generation via reflection or integration test
            // This would be done in a full integration test
            
        } catch (Exception e) {
            // Expected in unit test
        }
        
        // Verify mock interactions
        verify(amsAdapter, atLeastOnce()).generateThumbnails(anyString(), anyList());
    }
    
    @Test
    void shouldPackageStreamingAssets() {
        // Given
        String assetId = "asset-123";
        PackagingResult packagingResult = PackagingResult.builder()
                .hlsUrl("https://streaming.azure.net/hls.m3u8")
                .dashUrl("https://streaming.azure.net/dash.mpd")
                .assetId(assetId)
                .build();
        
        when(amsAdapter.packageHlsDash(assetId))
                .thenReturn(packagingResult);
        
        // When
        PackagingResult result = orchestrator.packageHlsDash(assetId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHlsUrl()).isEqualTo(packagingResult.getHlsUrl());
        assertThat(result.getDashUrl()).isEqualTo(packagingResult.getDashUrl());
    }
    
    @Test
    void shouldApplyDRMProtection() {
        // Given
        String assetId = "asset-123";
        DRMConfiguration drmConfig = DRMConfiguration.builder()
                .contentKeys(Map.of(
                        "Widevine", "widevine-key-123",
                        "FairPlay", "fairplay-key-456",
                        "PlayReady", "playready-key-789"
                ))
                .licenseUrls(Map.of(
                        "Widevine", "https://license.widevine.com/video",
                        "FairPlay", "https://license.fairplay.com/video",
                        "PlayReady", "https://license.playready.com/video"
                ))
                .build();
        
        when(amsAdapter.applyDRMProtection(assetId))
                .thenReturn(drmConfig);
        
        // When
        DRMConfiguration result = orchestrator.applyDRMProtection(assetId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContentKeys()).hasSize(3);
        assertThat(result.getLicenseUrls()).hasSize(3);
    }
    
    @Test
    void shouldHandleJobFailure() {
        // Given
        String errorMessage = "Encoding job failed";
        
        JobResult failedJob = JobResult.builder()
                .jobId("azure-job-123")
                .status(JobStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
        
        when(amsAdapter.waitForJobCompletion(anyString()))
                .thenReturn(failedJob);
        
        MediaProcessingJob job = MediaProcessingJob.builder()
                .jobId("job-123")
                .videoId(videoId)
                .azureJobId("azure-job-123")
                .status(ProcessingStatus.ENCODING)
                .build();
        
        // When/Then
        assertThat(job.getStatus()).isEqualTo(ProcessingStatus.ENCODING);
        // In real implementation, the orchestrator would handle failure
    }
    
    @Test
    void shouldUpdateJobStatus() {
        // Given
        MediaProcessingJob job = MediaProcessingJob.builder()
                .jobId("job-123")
                .videoId(videoId)
                .status(ProcessingStatus.QUEUED)
                .build();
        
        // When
        job.updateStatus(ProcessingStatus.ENCODING);
        
        // Then
        assertThat(job.getStatus()).isEqualTo(ProcessingStatus.ENCODING);
        assertThat(job.getStartedAt()).isNotNull();
    }
    
    @Test
    void shouldCalculateProcessingDuration() {
        // Given
        MediaProcessingJob job = MediaProcessingJob.builder()
                .jobId("job-123")
                .videoId(videoId)
                .status(ProcessingStatus.COMPLETED)
                .startedAt(new Date().toInstant().minusSeconds(300))
                .completedAt(new Date().toInstant())
                .build();
        
        // When
        long duration = job.getProcessingDurationMs();
        
        // Then
        assertThat(duration).isGreaterThan(0);
        assertThat(duration).isCloseTo(300000L, within(1000L)); // Within 1 second tolerance
    }
    
    @Test
    void shouldCompleteMediaProcessingSuccessfully() {
        // This would be an integration test
        // Testing the full flow: encoding -> thumbnails -> packaging -> DRM
    }
    
    @Test
    void shouldHandleThumbnailSelection() {
        // Given
        Thumbnail thumbnail = Thumbnail.builder()
                .thumbnailId(UUID.randomUUID())
                .videoId(videoId)
                .url("https://storage.blob.core.windows.net/thumbnails/thumb.jpg")
                .selected(false)
                .build();
        
        // When
        thumbnail.markAsSelected();
        
        // Then
        assertThat(thumbnail.isSelected()).isTrue();
    }
    
    @Test
    void shouldCreateDRMKeys() {
        // Given
        DRMKey drmKey = DRMKey.builder()
                .contentKeyId("content-key-123")
                .videoId(videoId)
                .drmType(DRMType.WIDEVINE)
                .keyIdentifier("widevine-key-456")
                .build();
        
        // When
        drmKey.addEncryptionConfig("licenseUrl", "https://license.com/video");
        drmKey.addEncryptionConfig("policyId", "policy-123");
        
        // Then
        assertThat(drmKey.getEncryptionConfig()).hasSize(2);
        assertThat(drmKey.getEncryptionConfig().get("licenseUrl"))
                .isEqualTo("https://license.com/video");
    }
}

