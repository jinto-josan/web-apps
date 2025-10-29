package com.youtube.captionssubtitlesservice.domain.services;

import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;

/**
 * Domain service interface for caption translation
 */
public interface TranslationService {
    
    /**
     * Translate caption content to target language
     * @param sourceContent Source caption content
     * @param sourceLanguage Source language
     * @param targetLanguage Target language
     * @return Translated caption content
     */
    String translate(String sourceContent, LanguageCode sourceLanguage, LanguageCode targetLanguage);
    
    /**
     * Translate entire subtitle file
     * @param fileContent Subtitle file content (SRT/WebVTT)
     * @param sourceLanguage Source language
     * @param targetLanguage Target language
     * @return Translated subtitle file content
     */
    String translateFile(String fileContent, LanguageCode sourceLanguage, LanguageCode targetLanguage);
    
    /**
     * Check if translation service is available
     * @return true if service is available
     */
    boolean isAvailable();
}
