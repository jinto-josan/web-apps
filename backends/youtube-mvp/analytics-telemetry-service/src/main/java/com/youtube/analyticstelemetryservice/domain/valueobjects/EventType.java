package com.youtube.analyticstelemetryservice.domain.valueobjects;

import lombok.Value;

/**
 * Value object representing event type.
 * Immutable enum-like value object for type safety.
 */
@Value
public class EventType {
    String value;
    
    public EventType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Event type cannot be null or blank");
        }
        // Validate format: should be lowercase with dots (e.g., "video.view", "user.login")
        if (!value.matches("^[a-z]+(\\.[a-z]+)*$")) {
            throw new IllegalArgumentException("Event type must be lowercase with dots (e.g., 'video.view')");
        }
        this.value = value;
    }
    
    public static EventType of(String value) {
        return new EventType(value);
    }
    
    // Common event types
    public static final EventType VIDEO_VIEW = new EventType("video.view");
    public static final EventType VIDEO_LIKE = new EventType("video.like");
    public static final EventType VIDEO_COMMENT = new EventType("video.comment");
    public static final EventType USER_LOGIN = new EventType("user.login");
    public static final EventType USER_LOGOUT = new EventType("user.logout");
    public static final EventType PAGE_VIEW = new EventType("page.view");
    public static final EventType SEARCH_QUERY = new EventType("search.query");
    public static final EventType AD_CLICK = new EventType("ad.click");
    public static final EventType AD_IMPRESSION = new EventType("ad.impression");
}

