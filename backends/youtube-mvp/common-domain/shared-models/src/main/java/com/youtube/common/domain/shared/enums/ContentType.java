package com.youtube.common.domain.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the type of content.
 * 
 * <p>Used across multiple services:
 * <ul>
 *   <li>video-catalog-service - Content classification</li>
 *   <li>recommendations-service - Content recommendations</li>
 *   <li>policy-enforcement-service - Content policy rules</li>
 *   <li>monetization-service - Monetization eligibility</li>
 * </ul>
 */
public enum ContentType {
    /**
     * Standard video content.
     */
    VIDEO("video"),
    
    /**
     * Short-form video content (Shorts).
     */
    SHORT("short"),
    
    /**
     * Live stream content.
     */
    LIVE("live"),
    
    /**
     * Live stream replay/archive.
     */
    LIVE_REPLAY("live_replay"),
    
    /**
     * Video-on-demand content.
     */
    VOD("vod"),
    
    /**
     * Podcast audio content.
     */
    PODCAST("podcast"),
    
    /**
     * Music video.
     */
    MUSIC("music"),
    
    /**
     * Educational/tutorial content.
     */
    EDUCATIONAL("educational"),
    
    /**
     * Gaming content.
     */
    GAMING("gaming");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ContentType fromValue(String value) {
        for (ContentType type : ContentType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ContentType: " + value);
    }

    /**
     * Checks if the content is live content.
     * 
     * @return true if live or live replay
     */
    public boolean isLive() {
        return this == LIVE || this == LIVE_REPLAY;
    }

    /**
     * Checks if the content is short-form.
     * 
     * @return true if short
     */
    public boolean isShort() {
        return this == SHORT;
    }
}

