package com.youtube.captionssubtitlesservice.domain.valueobjects;

/**
 * Caption processing status
 */
public enum CaptionStatus {
    PENDING,      // Caption request created
    PROCESSING,   // STT or translation in progress
    COMPLETED,    // Caption ready for use
    FAILED,       // Processing failed
    DELETED       // Caption deleted (soft delete)
}
