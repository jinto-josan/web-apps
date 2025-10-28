package com.youtube.mvp.streaming.presentation.rest;

import com.youtube.mvp.streaming.application.dto.ManifestResponse;
import com.youtube.mvp.streaming.application.dto.TokenResponse;
import com.youtube.mvp.streaming.application.service.PlaybackService;
import com.youtube.mvp.streaming.application.service.TokenService;
import com.youtube.mvp.streaming.domain.model.DeviceInfo;
import com.youtube.mvp.streaming.domain.model.DeviceType;
import com.youtube.mvp.streaming.domain.model.VideoMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Playback controller for manifest and token generation.
 */
@RestController
@RequestMapping("/api/v1/playback")
@Tag(name = "Playback", description = "Video playback API")
@RequiredArgsConstructor
@Slf4j
public class PlaybackController {
    
    private final PlaybackService playbackService;
    private final TokenService tokenService;
    
    @GetMapping("/{videoId}/manifest")
    @Operation(summary = "Get manifest URL", description = "Returns streaming manifest URL with signed token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manifest URL generated"),
            @ApiResponse(responseCode = "403", description = "Access denied by policy"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    public ResponseEntity<ManifestResponse> getManifest(
            @PathVariable String videoId,
            HttpServletRequest request) {
        
        log.info("Getting manifest for video: {}", videoId);
        
        // Extract user ID from authentication
        String userId = getUserId();
        
        // Build device info from request
        DeviceInfo device = buildDeviceInfo(request);
        
        // Build video metadata (in production, fetch from catalog service)
        VideoMetadata video = VideoMetadata.builder()
                .videoId(videoId)
                .title("Sample Video")
                .videoFormat("hls")
                .visibility("PUBLIC")
                .duration(3600)
                .build();
        
        ManifestResponse response = playbackService.getManifestUrl(videoId, userId, device, video);
        
        // Cache headers for CDN
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).private())
                .eTag(response.getSessionId())
                .body(response);
    }
    
    @GetMapping("/{videoId}/token")
    @Operation(summary = "Get playback token", description = "Returns JWT token for secure playback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token generated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<TokenResponse> getToken(
            @PathVariable String videoId,
            HttpServletRequest request) {
        
        log.info("Getting token for video: {}", videoId);
        
        String userId = getUserId();
        DeviceInfo device = buildDeviceInfo(request);
        
        TokenResponse response = tokenService.generatePlaybackToken(userId, videoId, device);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
    
    /**
     * Builds device info from HTTP request.
     */
    private DeviceInfo buildDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);
        
        // Extract device type from user agent (simplified)
        DeviceType deviceType = extractDeviceType(userAgent);
        
        return DeviceInfo.builder()
                .deviceId(generateDeviceId(request))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .countryCode("US") // Would use GeoIP in production
                .region("North America")
                .deviceType(deviceType)
                .os(extractOS(userAgent))
                .browser(extractBrowser(userAgent))
                .build();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    private DeviceType extractDeviceType(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android")) return DeviceType.MOBILE;
        if (ua.contains("tablet") || ua.contains("ipad")) return DeviceType.TABLET;
        if (ua.contains("smart-tv") || ua.contains("tv")) return DeviceType.TV;
        return DeviceType.DESKTOP;
    }
    
    private String extractOS(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac")) return "macOS";
        if (ua.contains("linux")) return "Linux";
        if (ua.contains("android")) return "Android";
        if (ua.contains("ios")) return "iOS";
        return "Unknown";
    }
    
    private String extractBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("chrome")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari")) return "Safari";
        if (ua.contains("edge")) return "Edge";
        return "Unknown";
    }
    
    private String generateDeviceId(HttpServletRequest request) {
        // In production, use device fingerprinting
        return request.getHeader("X-Device-ID") != null ? 
               request.getHeader("X-Device-ID") : 
               "device-" + request.getRemoteAddr().hashCode();
    }
    
    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName(); // Usually the user ID
        }
        return "anonymous";
    }
}

