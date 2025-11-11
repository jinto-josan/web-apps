package com.youtube.userprofileservice.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO containing the photo upload URL and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadResponse {
    private String uploadUrl;
    private String blobName;
    private String containerName;
    private Instant expiresAt;
    private Long maxFileSizeBytes;
    private Integer durationMinutes;
}


