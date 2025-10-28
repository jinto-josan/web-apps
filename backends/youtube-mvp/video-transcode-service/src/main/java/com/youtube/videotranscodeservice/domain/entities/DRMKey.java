package com.youtube.videotranscodeservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DRMKey {
    private String contentKeyId;
    private UUID videoId;
    private DRMType drmType;
    private String keyIdentifier;
    private Map<String, String> encryptionConfig;
    private Instant createdAt;
    
    public void addEncryptionConfig(String key, String value) {
        if (this.encryptionConfig == null) {
            this.encryptionConfig = new HashMap<>();
        }
        this.encryptionConfig.put(key, value);
    }
}

