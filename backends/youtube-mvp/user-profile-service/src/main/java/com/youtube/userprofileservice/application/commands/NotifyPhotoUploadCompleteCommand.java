package com.youtube.userprofileservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for notifying that a photo upload is complete.
 * Follows CQRS pattern - this is a write operation that triggers processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyPhotoUploadCompleteCommand {
    private String accountId;
    private String blobName;
    private String containerName;
    private String contentType;
    private Long fileSizeBytes;
}


