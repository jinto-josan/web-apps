package com.youtube.userprofileservice.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Message sent to the photo processing queue after upload completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadedMessage {
    
    @JsonProperty("accountId")
    private String accountId;
    
    @JsonProperty("blobName")
    private String blobName;
    
    @JsonProperty("containerName")
    private String containerName;
    
    @JsonProperty("contentType")
    private String contentType;
    
    @JsonProperty("uploadedAt")
    private Instant uploadedAt;
    
    @JsonProperty("fileSizeBytes")
    private Long fileSizeBytes;
}


