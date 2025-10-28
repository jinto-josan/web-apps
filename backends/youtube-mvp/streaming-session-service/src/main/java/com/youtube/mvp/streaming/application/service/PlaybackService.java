package com.youtube.mvp.streaming.application.service;

import com.youtube.mvp.streaming.application.dto.ManifestResponse;
import com.youtube.mvp.streaming.domain.model.*;
import com.youtube.mvp.streaming.domain.repository.PlaybackSessionRepository;
import com.youtube.mvp.streaming.domain.service.PolicyEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Playback service for session management and manifest generation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlaybackService {
    
    private final PlaybackSessionRepository sessionRepository;
    private final PolicyEngine policyEngine;
    private final TokenService tokenService;
    
    @Value("${cdn.base-url}")
    private String cdnBaseUrl;
    
    @Value("${video.format:hls}")
    private String defaultVideoFormat;
    
    /**
     * Gets or creates a playback session and returns manifest URL.
     */
    @Transactional
    public ManifestResponse getManifestUrl(String videoId, String userId, DeviceInfo device, VideoMetadata video) {
        log.info("Getting manifest URL for video: {}, user: {}", videoId, userId);
        
        // Check if session exists
        Optional<PlaybackSession> existing = sessionRepository.findByVideoIdAndUserId(videoId, userId);
        
        PlaybackSession session;
        if (existing.isPresent() && !existing.get().isExpired()) {
            session = existing.get();
            session.updateActivity();
        } else {
            // Create new session
            session = PlaybackSession.create(videoId, userId, device);
            
            // Evaluate policies
            PolicyResult policyResult = policyEngine.evaluatePlaybackPolicy(session, device, video);
            
            if (!policyResult.isAllowed()) {
                session.recordPolicyCheck(policyResult.getPolicyName(), false, policyResult.getReason());
                session = session.toBuilder()
                        .status(PlaybackStatus.BLOCKED)
                        .build();
                sessionRepository.save(session);
                
                throw new IllegalArgumentException("Playback not allowed: " + policyResult.getReason());
            }
            
            session.recordPolicyCheck(policyResult.getPolicyName(), true, null);
        }
        
        // Generate token
        TokenResponse tokenResponse = tokenService.generatePlaybackToken(userId, videoId, device);
        
        // Generate manifest URL
        String manifestUrl = generateManifestUrl(videoId, video.getVideoFormat() != null ? video.getVideoFormat() : defaultVideoFormat);
        
        // Update session with manifest and token
        session = session.toBuilder()
                .manifestUrl(manifestUrl)
                .token(tokenResponse.getToken())
                .build();
        
        sessionRepository.save(session);
        
        log.info("Generated manifest for session: {}", session.getSessionId());
        
        return ManifestResponse.builder()
                .manifestUrl(manifestUrl)
                .token(tokenResponse.getToken())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .videoId(videoId)
                .sessionId(session.getSessionId())
                .cdnBaseUrl(cdnBaseUrl)
                .build();
    }
    
    /**
     * Generates manifest URL based on format.
     */
    private String generateManifestUrl(String videoId, String format) {
        switch (format.toLowerCase()) {
            case "hls":
                return String.format("%s/videos/%s/master.m3u8", cdnBaseUrl, videoId);
            case "dash":
                return String.format("%s/videos/%s/manifest.mpd", cdnBaseUrl, videoId);
            case "mp4":
                return String.format("%s/videos/%s/video.mp4", cdnBaseUrl, videoId);
            default:
                throw new IllegalArgumentException("Unsupported video format: " + format);
        }
    }
    
    /**
     * Gets existing session.
     */
    public Optional<PlaybackSession> getSession(String videoId, String userId) {
        return sessionRepository.findByVideoIdAndUserId(videoId, userId);
    }
    
    /**
     * Records bytes delivered.
     */
    @Transactional
    public void recordBytesDelivered(String sessionId, long bytes) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.recordBytesDelivered(bytes);
            sessionRepository.save(session);
        });
    }
}

