package com.youtube.mvp.streaming.domain.service.impl;

import com.youtube.mvp.streaming.domain.model.*;
import com.youtube.mvp.streaming.domain.service.PolicyEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PolicyEngineImplTest {
    
    @InjectMocks
    private PolicyEngineImpl policyEngine;
    
    private DeviceInfo deviceInfo;
    private VideoMetadata videoMetadata;
    private PlaybackSession session;
    
    @BeforeEach
    void setUp() {
        deviceInfo = DeviceInfo.builder()
                .deviceId("device-123")
                .userAgent("Mozilla/5.0")
                .ipAddress("192.168.1.1")
                .countryCode("US")
                .region("North America")
                .deviceType(DeviceType.DESKTOP)
                .os("Windows")
                .browser("Chrome")
                .build();
        
        videoMetadata = VideoMetadata.builder()
                .videoId("video-123")
                .title("Test Video")
                .videoFormat("hls")
                .drmType("widevine")
                .visibility("PUBLIC")
                .duration(3600)
                .allowedRegions(Arrays.asList("US", "CA", "MX"))
                .build();
        
        session = PlaybackSession.builder()
                .sessionId("session-123")
                .videoId("video-123")
                .userId("user-123")
                .status(PlaybackStatus.ACTIVE)
                .build();
    }
    
    @Test
    void evaluatePlaybackPolicy_shouldAllowWhenAllChecksPass() {
        // When
        PolicyResult result = policyEngine.evaluatePlaybackPolicy(session, deviceInfo, videoMetadata);
        
        // Then
        assertThat(result.isAllowed()).isTrue();
    }
    
    @Test
    void checkGeoRestrictions_shouldDenyWhenRegionBlocked() {
        // Given
        VideoMetadata blockedVideo = videoMetadata.toBuilder()
                .blockedRegions(Arrays.asList("US"))
                .build();
        
        // When
        boolean allowed = policyEngine.checkGeoRestrictions(deviceInfo, blockedVideo);
        
        // Then
        assertThat(allowed).isFalse();
    }
    
    @Test
    void checkGeoRestrictions_shouldDenyWhenNotInAllowedList() {
        // Given
        VideoMetadata restrictedVideo = videoMetadata.toBuilder()
                .allowedRegions(Arrays.asList("GB", "FR", "DE"))
                .build();
        
        // When
        boolean allowed = policyEngine.checkGeoRestrictions(deviceInfo, restrictedVideo);
        
        // Then
        assertThat(allowed).isFalse();
    }
    
    @Test
    void checkDeviceCompatibility_shouldAllowForSupportedFormats() {
        // When
        boolean compatible = policyEngine.checkDeviceCompatibility(deviceInfo, "hls");
        
        // Then
        assertThat(compatible).isTrue();
    }
    
    @Test
    void checkDrmRequirements_shouldAllowWidevineForModernDevices() {
        // Given
        VideoMetadata drmVideo = videoMetadata.toBuilder()
                .drmType("widevine")
                .build();
        
        // When
        boolean supports = policyEngine.checkDrmRequirements(deviceInfo, drmVideo);
        
        // Then
        assertThat(supports).isTrue();
    }
}

