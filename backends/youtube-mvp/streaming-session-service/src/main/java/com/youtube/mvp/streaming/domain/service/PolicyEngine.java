package com.youtube.mvp.streaming.domain.service;

import com.youtube.mvp.streaming.domain.model.DeviceInfo;
import com.youtube.mvp.streaming.domain.model.PlaybackSession;
import com.youtube.mvp.streaming.domain.model.VideoMetadata;

/**
 * Policy engine interface for ABAC (Attribute-Based Access Control).
 */
public interface PolicyEngine {
    
    /**
     * Evaluates if user can playback video based on policies.
     * @param session Playback session
     * @param device Device information
     * @param video Video metadata
     * @return true if allowed, false otherwise
     */
    PolicyResult evaluatePlaybackPolicy(PlaybackSession session, DeviceInfo device, VideoMetadata video);
    
    /**
     * Checks geo restrictions.
     */
    boolean checkGeoRestrictions(DeviceInfo device, VideoMetadata video);
    
    /**
     * Checks device compatibility.
     */
    boolean checkDeviceCompatibility(DeviceInfo device, String videoFormat);
    
    /**
     * Checks time-based restrictions.
     */
    boolean checkTimeRestrictions(VideoMetadata video);
    
    /**
     * Checks DRM requirements.
     */
    boolean checkDrmRequirements(DeviceInfo device, VideoMetadata video);
}

