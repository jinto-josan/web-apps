package com.youtube.userprofileservice.domain.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.common.domain.core.DomainEvent;

import java.time.Instant;

/**
 * Domain event published when a photo upload is complete.
 * Triggers virus scanning and compression processing.
 */
public final class PhotoUploadedEvent extends DomainEvent {
    
    private final String accountId;
    private final String blobName;
    private final String containerName;
    private final String contentType;
    private final Long fileSizeBytes;

    @JsonCreator
    public PhotoUploadedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("accountId") String accountId,
            @JsonProperty("blobName") String blobName,
            @JsonProperty("containerName") String containerName,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("fileSizeBytes") Long fileSizeBytes) {
        super(eventId, occurredAt);
        this.accountId = accountId;
        this.blobName = blobName;
        this.containerName = containerName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
    }

    public PhotoUploadedEvent(String accountId, String blobName, String containerName,
                             String contentType, Long fileSizeBytes) {
        this.accountId = accountId;
        this.blobName = blobName;
        this.containerName = containerName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getBlobName() {
        return blobName;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    @Override
    public String getEventType() {
        return "photo.uploaded";
    }
}


