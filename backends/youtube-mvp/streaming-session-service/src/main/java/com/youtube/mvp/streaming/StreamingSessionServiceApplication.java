package com.youtube.mvp.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Streaming Session Service Application.
 * 
 * Features:
 * - Session management with Redis
 * - CDN integration (Azure Front Door + Blob Storage)
 * - DRM support (Widevine, PlayReady, FairPlay)
 * - ABAC policy engine
 * - JWT token generation
 * - Manifest generation (HLS/DASH)
 * - GeoIP restrictions
 * - Rate limiting
 */
@SpringBootApplication
public class StreamingSessionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamingSessionServiceApplication.class, args);
    }
}

