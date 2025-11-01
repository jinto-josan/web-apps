package com.youtube.common.domain.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the status of a video.
 * 
 * <p>Used across multiple services:
 * <ul>
 *   <li>video-catalog-service - Video lifecycle</li>
 *   <li>video-upload-service - Upload process</li>
 *   <li>video-transcode-service - Processing status</li>
 *   <li>streaming-session-service - Playback availability</li>
 * </ul>
 */
public enum VideoStatus {
    /**
     * Video is being uploaded.
     */
    UPLOADING("uploading"),
    
    /**
     * Video is being processed/transcoded.
     */
    PROCESSING("processing"),
    
    /**
     * Video processing has failed.
     */
    PROCESSING_FAILED("processing_failed"),
    
    /**
     * Video is published and available for viewing.
     */
    PUBLISHED("published"),
    
    /**
     * Video is scheduled for future publication.
     */
    SCHEDULED("scheduled"),
    
    /**
     * Video is private (only owner can view).
     */
    PRIVATE("private"),
    
    /**
     * Video is unlisted (accessible via direct link only).
     */
    UNLISTED("unlisted"),
    
    /**
     * Video has been deleted.
     */
    DELETED("deleted"),
    
    /**
     * Video has been taken down due to policy violation.
     */
    TAKEN_DOWN("taken_down");

    private final String value;

    VideoStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static VideoStatus fromValue(String value) {
        for (VideoStatus status : VideoStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown VideoStatus: " + value);
    }

    /**
     * Checks if the video is available for viewing.
     * 
     * @return true if published, unlisted, or private
     */
    public boolean isViewable() {
        return this == PUBLISHED || this == UNLISTED || this == PRIVATE;
    }

    /**
     * Checks if the video is in a processing state.
     * 
     * @return true if uploading or processing
     */
    public boolean isProcessing() {
        return this == UPLOADING || this == PROCESSING;
    }

    /**
     * Checks if the video is unavailable (deleted or taken down).
     * 
     * @return true if deleted or taken down
     */
    public boolean isUnavailable() {
        return this == DELETED || this == TAKEN_DOWN || this == PROCESSING_FAILED;
    }
}

