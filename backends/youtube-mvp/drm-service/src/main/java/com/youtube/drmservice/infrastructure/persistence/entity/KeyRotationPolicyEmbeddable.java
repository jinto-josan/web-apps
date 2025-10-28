package com.youtube.drmservice.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Duration;
import java.time.Instant;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyRotationPolicyEmbeddable {
    private Boolean enabled;
    
    @Column(name = "rotation_interval_seconds")
    private Long rotationIntervalSeconds;
    
    private Instant lastRotationAt;
    private Instant nextRotationAt;
    private String rotationKeyVaultUri;
    
    public Duration getRotationInterval() {
        return rotationIntervalSeconds != null 
            ? Duration.ofSeconds(rotationIntervalSeconds) 
            : null;
    }
    
    public void setRotationInterval(Duration interval) {
        this.rotationIntervalSeconds = interval != null ? interval.getSeconds() : null;
    }
}

