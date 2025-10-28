package com.youtube.mediaassist.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Value object representing a signed URL for secure blob access
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignedUrl {
    
    private String url;
    private Instant expiresAt;
    private String blobPath;
    private SignedUrlType type;
    
    public enum SignedUrlType {
        READ,
        WRITE,
        PLAYBACK
    }
}

