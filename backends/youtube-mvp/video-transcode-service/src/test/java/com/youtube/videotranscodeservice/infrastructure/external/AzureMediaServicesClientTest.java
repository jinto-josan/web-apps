package com.youtube.videotranscodeservice.infrastructure.external;

import com.youtube.videotranscodeservice.domain.valueobjects.*;
import com.azure.resourcemanager.media.MediaServicesManager;
import com.azure.resourcemanager.media.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureMediaServicesClientTest {
    
    @Mock
    private MediaServicesManager mediaServicesManager;
    
    @InjectMocks
    private AzureMediaServicesClient client;
    
    private String transformId;
    private String jobId;
    private String assetId;
    
    @BeforeEach
    void setUp() {
        transformId = "transform-123";
        jobId = "azure-job-123";
        assetId = "asset-123";
    }
    
    @Test
    void shouldCreateTransformSuccessfully() {
        // Given
        TransformConfig config = TransformConfig.builder()
                .name("H264AdaptiveBitrateMP4Set")
                .description("Adaptive bitrate encoding")
                .encodingPresets(Map.of("profile", "H264MultipleBitrate1080p"))
                .build();
        
        // When
        String result = client.createTransform(config);
        
        // Then
        assertThat(result).isNotNull();
        // In integration test, verify transform is created in Azure
    }
    
    @Test
    void shouldSubmitEncodingJob() {
        // Given
        String inputAssetUrl = "https://storage.blob.core.windows.net/videos/input.mp4";
        
        // When
        String result = client.submitJob(transformId, inputAssetUrl);
        
        // Then
        assertThat(result).isNotNull();
        // In integration test, verify job is submitted to Azure
    }
    
    @Test
    void shouldGetJobStatus() {
        // Given
        when(client.getJobStatus(jobId)).thenReturn(JobStatus.PROCESSING);
        
        // When
        JobStatus status = client.getJobStatus(jobId);
        
        // Then
        assertThat(status).isNotNull();
        assertThat(status).isIn(JobStatus.QUEUED, JobStatus.PROCESSING, JobStatus.FINISHED, JobStatus.FAILED);
    }
    
    @Test
    void shouldWaitForJobCompletion() {
        // Given
        JobResult result = JobResult.builder()
                .jobId(jobId)
                .status(JobStatus.FINISHED)
                .assetId(assetId)
                .build();
        
        when(client.waitForCompletion(jobId)).thenReturn(result);
        
        // When
        JobResult jobResult = client.waitForCompletion(jobId);
        
        // Then
        assertThat(jobResult).isNotNull();
        assertThat(jobResult.getStatus()).isEqualTo(JobStatus.FINISHED);
        assertThat(jobResult.getAssetId()).isEqualTo(assetId);
    }
    
    @Test
    void shouldGenerateThumbnails() {
        // Given
        List<String> timeCodes = Arrays.asList("00:00:03", "00:00:10", "00:00:20");
        List<String> thumbnailUrls = Arrays.asList(
                "https://storage.blob.core.windows.net/thumbnails/thumb1.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb2.jpg",
                "https://storage.blob.core.windows.net/thumbnails/thumb3.jpg"
        );
        
        when(client.generateThumbnails(assetId, timeCodes))
                .thenReturn(thumbnailUrls);
        
        // When
        List<String> result = client.generateThumbnails(assetId, timeCodes);
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsAll(thumbnailUrls);
    }
    
    @Test
    void shouldPackageForStreaming() {
        // Given
        PackagingResult expectedResult = PackagingResult.builder()
                .hlsUrl("https://streaming.azure.net/hls.m3u8")
                .dashUrl("https://streaming.azure.net/dash.mpd")
                .assetId(assetId)
                .build();
        
        when(client.packageForStreaming(assetId))
                .thenReturn(expectedResult);
        
        // When
        PackagingResult result = client.packageForStreaming(assetId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHlsUrl()).isEqualTo("https://streaming.azure.net/hls.m3u8");
        assertThat(result.getDashUrl()).isEqualTo("https://streaming.azure.net/dash.mpd");
    }
    
    @Test
    void shouldApplyDRMProtection() {
        // Given
        Map<String, String> contentKeys = new HashMap<>();
        contentKeys.put("Widevine", "widevine-key-123");
        contentKeys.put("FairPlay", "fairplay-key-456");
        contentKeys.put("PlayReady", "playready-key-789");
        
        Map<String, String> licenseUrls = new HashMap<>();
        licenseUrls.put("Widevine", "https://license.widevine.com/video");
        licenseUrls.put("FairPlay", "https://license.fairplay.com/video");
        licenseUrls.put("PlayReady", "https://license.playready.com/video");
        
        DRMConfiguration expectedConfig = DRMConfiguration.builder()
                .contentKeys(contentKeys)
                .licenseUrls(licenseUrls)
                .build();
        
        when(client.applyDRMProtection(assetId))
                .thenReturn(expectedConfig);
        
        // When
        DRMConfiguration result = client.applyDRMProtection(assetId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContentKeys()).hasSize(3);
        assertThat(result.getLicenseUrls()).hasSize(3);
        assertThat(result.getContentKeys().get("Widevine")).isEqualTo("widevine-key-123");
    }
    
    @Test
    void shouldHandleJobFailure() {
        // Given
        JobResult failedResult = JobResult.builder()
                .jobId(jobId)
                .status(JobStatus.FAILED)
                .errorMessage("Encoding failed: insufficient resources")
                .build();
        
        when(client.waitForCompletion(jobId))
                .thenReturn(failedResult);
        
        // When
        JobResult result = client.waitForCompletion(jobId);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(JobStatus.FAILED);
        assertThat(result.getErrorMessage()).isNotBlank();
    }
    
    @Test
    void shouldGetStreamingLocatorUrl() {
        // Given
        String expectedUrl = "https://streaming.azure.net/locator/12345";
        
        when(client.getStreamingLocatorUrl(assetId))
                .thenReturn(expectedUrl);
        
        // When
        String url = client.getStreamingLocatorUrl(assetId);
        
        // Then
        assertThat(url).isEqualTo(expectedUrl);
    }
}

