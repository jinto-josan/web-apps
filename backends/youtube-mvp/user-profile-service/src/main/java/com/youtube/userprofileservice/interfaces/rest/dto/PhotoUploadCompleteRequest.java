package com.youtube.userprofileservice.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for notifying the service that a photo upload is complete.
 * This triggers virus scanning and compression processing.
 */
@Data
public class PhotoUploadCompleteRequest {
    
    @NotBlank(message = "Blob name is required")
    private String blobName;
    
    @NotBlank(message = "Content type is required")
    private String contentType;
    
    @NotNull(message = "File size is required")
    private Long fileSizeBytes;
}


