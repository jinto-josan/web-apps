package com.youtube.userprofileservice.interfaces.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for generating a photo upload URL.
 */
@Data
public class PhotoUploadRequest {
    
    @NotBlank(message = "Content type is required")
    private String contentType; // e.g., "image/jpeg", "image/png"
    
    @NotNull(message = "Max file size is required")
    @Min(value = 1024, message = "Max file size must be at least 1KB")
    @Max(value = 10485760, message = "Max file size must not exceed 10MB")
    private Long maxFileSizeBytes; // Maximum file size in bytes (default: 10MB)
}


