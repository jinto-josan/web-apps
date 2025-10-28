package com.youtube.mvp.streaming.application.service;

import com.youtube.mvp.streaming.application.dto.TokenResponse;
import com.youtube.mvp.streaming.domain.model.DeviceInfo;
import com.youtube.mvp.streaming.domain.model.VideoMetadata;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT token service for signed URLs.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {
    
    @Value("${jwt.secret:default-secret-key-change-in-production}")
    private String jwtSecret;
    
    @Value("${jwt.issuer:streaming-service}")
    private String jwtIssuer;
    
    @Value("${jwt.expiration-hours:1}")
    private int expirationHours;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Generates a JWT token for playback.
     */
    public TokenResponse generatePlaybackToken(String userId, String videoId, DeviceInfo device) {
        log.debug("Generating playback token for user: {}, video: {}", userId, videoId);
        
        Instant expiration = Instant.now().plus(expirationHours, ChronoUnit.HOURS);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("videoId", videoId);
        claims.put("deviceId", device.getDeviceId());
        claims.put("ip", device.getIpAddress());
        claims.put("country", device.getCountryCode());
        claims.put("type", "playback");
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtIssuer)
                .setSubject(userId)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        
        log.debug("Generated token, expires at: {}", expiration);
        
        return TokenResponse.builder()
                .token(token)
                .expiresAt(expiration)
                .videoId(videoId)
                .type("playback")
                .build();
    }
    
    /**
     * Validates a JWT token.
     */
    public boolean validateToken(String token, String expectedIp, String expectedDeviceId) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Check IP binding
            String ip = (String) claims.get("ip");
            if (ip != null && !ip.equals(expectedIp)) {
                log.warn("IP mismatch: expected {}, got {}", expectedIp, ip);
                return false;
            }
            
            // Check device binding
            String deviceId = (String) claims.get("deviceId");
            if (deviceId != null && !deviceId.equals(expectedDeviceId)) {
                log.warn("Device mismatch: expected {}, got {}", expectedDeviceId, deviceId);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
    
    /**
     * Extracts video ID from token.
     */
    public String extractVideoId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return (String) claims.get("videoId");
        } catch (Exception e) {
            log.error("Failed to extract video ID from token", e);
            return null;
        }
    }
}

