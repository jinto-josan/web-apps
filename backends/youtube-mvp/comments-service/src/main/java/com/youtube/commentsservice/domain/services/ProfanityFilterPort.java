package com.youtube.commentsservice.domain.services;

/**
 * Port for profanity filtering service
 * Implemented by infrastructure layer
 */
public interface ProfanityFilterPort {
    
    /**
     * Check if text contains profanity
     * @param text the text to check
     * @return true if profanity detected, false otherwise
     */
    boolean containsProfanity(String text);
    
    /**
     * Filter profanity from text
     * @param text the text to filter
     * @return filtered text
     */
    String filterProfanity(String text);
}

