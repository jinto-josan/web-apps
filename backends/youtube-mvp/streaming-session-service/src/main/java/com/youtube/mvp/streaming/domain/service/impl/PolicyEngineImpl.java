package com.youtube.mvp.streaming.domain.service.impl;

import com.youtube.mvp.streaming.domain.model.*;
import com.youtube.mvp.streaming.domain.service.PolicyEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Policy engine implementation with ABAC logic.
 */
@Service
@Slf4j
public class PolicyEngineImpl implements PolicyEngine {
    
    @Override
    public PolicyResult evaluatePlaybackPolicy(PlaybackSession session, DeviceInfo device, VideoMetadata video) {
        log.debug("Evaluating playback policy for video: {}, user: {}", video.getVideoId(), session.getUserId());
        
        // Check geo restrictions
        if (!checkGeoRestrictions(device, video)) {
            return PolicyResult.denied("geo-restriction", 
                    "Video not available in region: " + device.getCountryCode());
        }
        
        // Check device compatibility
        if (!checkDeviceCompatibility(device, video.getVideoFormat())) {
            return PolicyResult.denied("device-incompatible", 
                    "Device not compatible with format: " + video.getVideoFormat());
        }
        
        // Check time restrictions
        if (!checkTimeRestrictions(video)) {
            return PolicyResult.denied("time-restriction", 
                    "Video not available at current time");
        }
        
        // Check DRM requirements
        if (video.getDrmType() != null && !video.getDrmType().isEmpty()) {
            if (!checkDrmRequirements(device, video)) {
                return PolicyResult.denied("drm-required", 
                        "DRM not supported on this device");
            }
        }
        
        log.debug("Playback policy evaluation passed");
        return PolicyResult.allowed("default");
    }
    
    @Override
    public boolean checkGeoRestrictions(DeviceInfo device, VideoMetadata video) {
        log.debug("Checking geo restrictions for country: {}", device.getCountryCode());
        
        // Check blocked regions
        if (video.getBlockedRegions() != null && 
            video.getBlockedRegions().contains(device.getCountryCode())) {
            log.warn("Region blocked: {}", device.getCountryCode());
            return false;
        }
        
        // Check allowed regions
        if (video.getAllowedRegions() != null && !video.getAllowedRegions().isEmpty()) {
            if (!video.getAllowedRegions().contains(device.getCountryCode())) {
                log.warn("Region not in allowed list: {}", device.getCountryCode());
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean checkDeviceCompatibility(DeviceInfo device, String videoFormat) {
        log.debug("Checking device compatibility for format: {}", videoFormat);
        
        // Basic compatibility checks
        switch (videoFormat.toLowerCase()) {
            case "hls":
                return true; // HLS is widely supported
            case "dash":
                return true; // DASH is widely supported
            case "mp4":
                return true; // MP4 is universally supported
            default:
                return false;
        }
    }
    
    @Override
    public boolean checkTimeRestrictions(VideoMetadata video) {
        log.debug("Checking time restrictions");
        // For now, no time restrictions
        // In production, check scheduled availability
        return true;
    }
    
    @Override
    public boolean checkDrmRequirements(DeviceInfo device, VideoMetadata video) {
        log.debug("Checking DRM requirements for type: {}", video.getDrmType());
        
        // Check if device supports required DRM
        switch (video.getDrmType().toLowerCase()) {
            case "widevine":
                // Widevine is supported on most modern devices
                return true;
            case "playready":
                // PlayReady supported on Windows, Xbox
                return device.getOs() != null && 
                       (device.getOs().toLowerCase().contains("windows") || 
                        device.getOs().toLowerCase().contains("xbox"));
            case "fairplay":
                // FairPlay supported on Apple devices
                return device.getOs() != null && 
                       device.getOs().toLowerCase().contains("ios") || 
                       device.getOs().toLowerCase().contains("mac");
            default:
                return false;
        }
    }
}

