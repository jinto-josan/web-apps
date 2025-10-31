package com.youtube.antiaabuseservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvent {
    private String id;
    private EventType eventType;
    private String userId;
    private String contentId;
    private Map<String, Object> context;
    private Instant timestamp;

    public enum EventType {
        VIEW, AD_VIEW, COMMENT, UPLOAD, SUBSCRIPTION, LIKE, DISLIKE
    }
}

