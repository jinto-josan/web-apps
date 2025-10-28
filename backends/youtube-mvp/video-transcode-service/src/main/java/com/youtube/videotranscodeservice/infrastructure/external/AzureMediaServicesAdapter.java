package com.youtube.videotranscodeservice.infrastructure.external;

import com.youtube.videotranscodeservice.domain.valueobjects.*;

import java.util.List;

public interface AzureMediaServicesAdapter {
    String createTransform(TransformConfig config);
    String submitEncodingJob(String transformId, String inputAssetUrl);
    JobStatus getJobStatus(String jobId);
    JobResult waitForJobCompletion(String jobId);
    List<String> generateThumbnails(String assetId, List<String> timeCodes);
    PackagingResult packageHlsDash(String assetId);
    DRMConfiguration applyDRMProtection(String assetId);
    String getStreamingLocatorUrl(String assetId);
}

