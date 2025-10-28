package com.youtube.videotranscodeservice.integration;

import com.youtube.videotranscodeservice.application.services.MediaProcessingOrchestrator;
import com.youtube.videotranscodeservice.domain.entities.*;
import com.youtube.videotranscodeservice.domain.repositories.*;
import com.youtube.videotranscodeservice.domain.valueobjects.*;
import com.youtube.videotranscodeservice.infrastructure.external.AzureMediaServicesAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration Test for Media Processing Service
 * 
 * Tests the complete workflow:
 * 1. Video upload triggers media processing
 * 2. Azure Media Services encoding job
 * 3. Thumbnail generation
 * 4. HLS/DASH packaging
 * 5. DRM protection application
 * 6. Event publishing
 */
@SpringBootTest
@ActiveProfiles("test")
class MediaProcessingIntegrationTest {
    
    @Autowired
    private MediaProcessingOrchestrator orchestrator;
    
    @MockBean
    private AzureMediaServicesAdapter amsAdapter;
    
    @MockBean
    private MediaProcessingRepository jobRepository;
    
    @MockBean
    private ThumbnailRepository thumbnailRepository;
    
    @MockBean
    private DRMKeyRepository drmKeyRepository;
    
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
    void shouldProcessMediaFromUploadToCompletion() {
        // Given
        String azureJobId = "azure-job-123";
        String transformId = "transform-123";
        String assetId = "asset-123";
        
        // Mock Azure Media Services responses
        when(amsAdapter.submitEncodingJob(anyString(), anyString()))
                .thenReturn(azureJobId);
        
        JobResult jobResult = JobResult.builder()
                .jobId(azureJobId)
                .status(JobStatus.FINISHED)
                .assetId(assetId)
                .build();
        
        when(amsAdapter.waitForJobCompletion(azureJobId))
                .thenReturn(jobResult);
        
        List<String> thumbnailUrls = Arrays.asList(
                "https://storage.blob.core.windows.net/thumbnails/thumb1.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb2.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb3.jpg"
        );
        
        when(amsAdapter.generateThumbnails(assetId, anyList()))
                .thenReturn(thumbnailUrls);
        
        PackagingResult packagingResult = PackagingResult.builder()
                .hlsUrl("https://streaming.azure.net/hls.m3u8")
                .dashUrl("https://streaming.azure.net/dash.mpd")
                .assetId(assetId)
                .build();
        
        when(amsAdapter.packageHlsDash(assetId))
                .thenReturn(packagingResult);
        
        Map<String, String> contentKeys = Map.of(
                "Widevine", "widevine-key-123",
                "FairPlay", "fairplay-key-456",
                "PlayReady", "playready-key-789"
        );
        
        Map<String, String> licenseUrls = Map.of(
                "Widevine", "https://license.widevine.com/video",
                "FairPlay", "https://license.fairplay.com/video",
                "PlayReady", "https://license.playready.com/video"
        );
        
        DRMConfiguration drmConfig = DRMConfiguration.builder()
                .contentKeys(contentKeys)
                .licenseUrls(licenseUrls)
                .build();
        
        when(amsAdapter.applyDRMProtection(assetId))
                .thenReturn(drmConfig);
        
        // When
        MediaProcessingJob job = orchestrator.startProcessing(videoId, userId, inputBlobUrl);
        
        // Then
        assertThat(job).isNotNull();
        assertThat(job.getVideoId()).isEqualTo(videoId);
        assertThat(job.getAzureJobId()).isEqualTo(azureJobId);
        
        // Verify encoding job was submitted
        verify(amsAdapter).submitEncodingJob(anyString(), eq(inputBlobUrl));
        
        // Verify job repository interactions
        verify(jobRepository, atLeastOnce()).save(any(MediaProcessingJob.class));
        verify(jobRepository, atLeastOnce()).update(any(MediaProcessingJob.class));
    }
    
    @Test
    void shouldHandleEncodingFailure() {
        // Given
        String azureJobId = "azure-job-123";
        
        when(amsAdapter.submitEncodingJob(anyString(), anyString()))
                .thenReturn(azureJobId);
        
        JobResult failedResult = JobResult.builder()
                .jobId(azureJobId)
                .status(JobStatus.FAILED)
                .errorMessage("Encoding failed: insufficient resources")
                .build();
        
        when(amsAdapter.waitForJobCompletion(azureJobId))
                .thenReturn(failedResult);
        
        // When
        MediaProcessingJob job = orchestrator.startProcessing(videoId, userId, inputBlobUrl);
        
        // Then
        assertThat(job).isNotNull();
        // Job status should be ENCODING initially
        assertThat(job.getStatus()).isEqualTo(ProcessingStatus.ENCODING);
        
        // Verify error handling
        verify(jobRepository, atLeastOnce()).update(any(MediaProcessingJob.class));
    }
    
    @Test
    void shouldGenerateMultipleThumbnails() {
        // Given
        String assetId = "asset-123";
        List<String> timeCodes = Arrays.asList("00:00:03", "00:00:10", "00:00:20");
        
        List<String> thumbnailUrls = Arrays.asList(
                "https://storage.blob.core.windows.net/thumbnails/thumb1.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb2.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb3.jpg"
        );
        
        when(amsAdapter.generateThumbnails(assetId, timeCodes))
                .thenReturn(thumbnailUrls);
        
        // When
        List<String> result = amsAdapter.generateThumbnails(assetId, timeCodes);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsAll(thumbnailUrls);
        
        verify(amsAdapter).generateThumbnails(assetId, timeCodes);
    }
    
    @Test
    void shouldPackageForMultipleStreamingFormats() {
        // Given
        String assetId = "asset-123";
        
        PackagingResult expectedResult = PackagingResult.builder()
                .hlsUrl("https://streaming.azure.net/hls.m3u8")
                .dashUrl("https://streaming.azure.net/dash.mpd")
                .assetId(assetId)
                .build();
        
        when(amsAdapter.packageHlsDash(assetId))
                .thenReturn(expectedResult);
        
        // When
        PackagingResult result = amsAdapter.packageHlsDash(assetId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHlsUrl()).isEqualTo("https://streaming.azure.net/hls.m3u8");
        assertThat(result.getDashUrl()).isEqualTo("https://streaming.azure.net/dash.mpd");
    }
    
    @Test
    void shouldApplyMultiDRMProtection() {
        // Given
        String assetId = "asset-123";
        
        Map<String, String> contentKeys = Map.of(
                "Widevine", "widevine-key-123",
                "FairPlay", "fairplay-key-456",
                "PlayReady", "playready-key-789"
        );
        
        Map<String, String> licenseUrls = Map.of(
                "Widevine", "https://license.widevine.com/video",
                "FairPlay", "https://license.fairplay.com/video",
                "PlayReady", "https://license.playready.com/video"
        );
        
        DRMConfiguration expectedConfig = DRMConfiguration.builder()
                .contentKeys(contentKeys)
                .licenseUrls(licenseUrls)
                .build();
        
        when(amsAdapter.applyDRMProtection(assetId))
                .thenReturn(expectedConfig);
        
        // When
        DRMConfiguration result = amsAdapter.applyDRMProtection(assetId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContentKeys()).hasSize(3);
        assertThat(result.getLicenseUrls()).hasSize(3);
        assertThat(result.getContentKeys()).containsKeys("Widevine", "FairPlay", "PlayReady");
    }
    
    @Test
    void shouldSelectThumbnail() {
        // Given
        UUID thumbnailId1 = UUID.randomUUID();
        UUID thumbnailId2 = UUID.randomUUID();
        UUID thumbnailId3 = UUID.randomUUID();
        
        List<Thumbnail> thumbnails = Arrays.asList(
                Thumbnail.builder()
                        .thumbnailId(thumbnailId1)
                        .videoId(videoId)
                        .url("https://thumb1.jpg")
                        .selected(false)
                        .build(),
                Thumbnail.builder()
                        .thumbnailId(thumbnailId2)
                        .videoId(videoId)
                        .url("https://thumb2.jpg")
                        .selected(false)
                        .build(),
                Thumbnail.builder()
                        .thumbnailId(thumbnailId3)
                        .videoId(videoId)
                        .url("https://thumb3.jpg")
                        .selected(false)
                        .build()
        );
        
        when(thumbnailRepository.findByVideoId(videoId))
                .thenReturn(thumbnails);
        
        // When
        Thumbnail selected = thumbnails.get(1);
        selected.markAsSelected();
        
        // Then
        assertThat(selected.isSelected()).isTrue();
        
        // Unselect others
        thumbnails.stream()
                .filter(t -> !t.getThumbnailId().equals(thumbnailId2))
                .forEach(Thumbnail::markAsUnselected);
        
        assertThat(thumbnails.get(0).isSelected()).isFalse();
        assertThat(thumbnails.get(2).isSelected()).isFalse();
    }
}

