package com.youtube.captionssubtitlesservice.domain.services;

import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;

/**
 * Domain service interface for Speech-to-Text operations
 */
public interface SpeechToTextService {
    
    /**
     * Generate captions from video audio using STT
     * @param videoId Video identifier
     * @param audioUri URI to audio/video file
     * @param language Expected language
     * @return Caption text content
     */
    String generateCaptions(String videoId, String audioUri, LanguageCode language);
    
    /**
     * Check if STT service is available
     * @return true if service is available
     */
    boolean isAvailable();
}
