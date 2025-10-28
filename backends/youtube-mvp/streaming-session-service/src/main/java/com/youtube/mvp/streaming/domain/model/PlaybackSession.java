package com.youtuube.mvp.streaming.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.*;

/**
 * Playback session aggregate root.
 * Manages streaming sessions with geo/policy checks.
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PlaybackSession {
    
    @EqualsAndHashCode.Include
    private final String sessionId;
    
    private String videoId;
    private String userId;
    private String deviceId;
    private String userAgent;
    private String ipAddress;
    private String countryCode;
    private String region;
    
    private PlaybackStatus status;
    private String manifestUrl;
    private String token;
    
    private Instant startedAt;
    private Instant expiresAt;
    private Instant lastActivity;
    
    @Builder.Default
    private long bytesDelivered = 0;
    
    private Map<String, String> metadata;
    private List<PolicyCheck> policyChecks;
    
    // DDD: Business logic methods
    
    /**
     * Creates a new playback session.
     */
    public static PlaybackSession create(String videoId, String userId, DeviceInfo device) {
        return PlaybackSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .videoId(videoId)
                .userId(userId)
                .deviceId(device.getDeviceId())
                .userAgent(device.getUserAgent())
                .ipAddress(device.getIpAddress())
                .countryCode(device.getCountryCode())
                .region(device.getRegion())
                .status(PlaybackStatus.ACTIVE)
                .startedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600)) // 1 hour
                .lastActivity(Instant.now())
                .metadata(new HashMap<>())
                .policyChecks(new ArrayList<>())
                .build();
    }
    
    /**
     * Records policy check result.
     */
    public void recordPolicyCheck(String policyName, boolean allowed, String reason) {
        if (policyChecks == null) {
            policyChecks = new ArrayList<>();
        }
        
        policyChecks.add(PolicyCheck.builder()
                .policyName(policyName)
                .allowed(allowed)
                .reason(reason)
                .checkedAt(Instant.now())
                .build());
    }
    
    /**
     * Checks if session is expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Updates last activity time.
     */
    public void updateActivity() {
        this.lastActivity = Instant.now();
    }
    
    /**
     * Records bytes delivered.
     */
    public void recordBytesDelivered(long bytes) {
        this.bytesDelivered += bytes;
        this.lastActivity = Instant.now();
    }
}

