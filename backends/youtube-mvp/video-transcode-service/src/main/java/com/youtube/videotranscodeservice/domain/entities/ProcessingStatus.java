package com.youtube.videotranscodeservice.domain.entities;

public enum ProcessingStatus {
    QUEUED,
    ENCODING,
    PACKAGING,
    DRM_PROCESSING,
    THUMBNAIL_GENERATING,
    COMPLETED,
    FAILED
}

