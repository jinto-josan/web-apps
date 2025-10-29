package com.youtube.captionssubtitlesservice.domain.valueobjects;

/**
 * Caption source type
 */
public enum SourceType {
    MANUAL,  // Uploaded by user
    AUTO,    // Auto-generated via STT
    TRANSLATED // Translated from another caption
}
