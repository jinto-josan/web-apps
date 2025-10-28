package com.youtube.mediaassist.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Set;

/**
 * Value object representing a SAS policy configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SasPolicy {
    
    private Duration validityDuration;
    private Set<SasPermission> permissions;
    private String ipRange;
    private SasProtocol protocol;
    
    public enum SasPermission {
        READ,
        WRITE,
        DELETE,
        LIST,
        ADD,
        CREATE,
        UPDATE,
        PROCESS
    }
    
    public enum SasProtocol {
        HTTPS_ONLY,
        HTTPS_HTTP
    }
    
    @Builder.Default
    private boolean enforceHttps = true;
    
    @Builder.Default
    private String cacheControl = "public, max-age=3600";
}

