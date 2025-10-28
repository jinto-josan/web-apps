package com.youtube.mvp.streaming.application.service;

import com.youtube.mvp.streaming.domain.model.DeviceInfo;
import com.youtube.mvp.streaming.domain.model.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    
    @InjectMocks
    private TokenService tokenService;
    
    private DeviceInfo deviceInfo;
    
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
    }
    
    @Test
    void generatePlaybackToken_shouldGenerateValidToken() {
        // When
        var tokenResponse = tokenService.generatePlaybackToken("user-123", "video-456", deviceInfo);
        
        // Then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getToken()).isNotBlank();
        assertThat(tokenResponse.getVideoId()).isEqualTo("video-456");
        assertThat(tokenResponse.getType()).isEqualTo("playback");
        assertThat(tokenResponse.getExpiresAt()).isNotNull();
    }
    
    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        // Given
        var tokenResponse = tokenService.generatePlaybackToken("user-123", "video-456", deviceInfo);
        
        // When
        boolean isValid = tokenService.validateToken(
                tokenResponse.getToken(),
                deviceInfo.getIpAddress(),
                deviceInfo.getDeviceId()
        );
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void validateToken_shouldReturnFalseForWrongIP() {
        // Given
        var tokenResponse = tokenService.generatePlaybackToken("user-123", "video-456", deviceInfo);
        
        // When
        boolean isValid = tokenService.validateToken(
                tokenResponse.getToken(),
                "192.168.1.999", // Wrong IP
                deviceInfo.getDeviceId()
        );
        
        // Then
        assertThat(isValid).isFalse();
    }
}

