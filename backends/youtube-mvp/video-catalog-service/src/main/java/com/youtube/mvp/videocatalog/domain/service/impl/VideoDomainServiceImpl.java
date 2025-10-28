package com.youtube.mvp.videocatalog.domain.service.impl;

import com.youtube.mvp.videocatalog.domain.model.Video;
import com.youtube.mvp.videocatalog.domain.service.VideoDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;

/**
 * Domain service implementation.
 */
@Service
@Slf4j
public class VideoDomainServiceImpl implements VideoDomainService {
    
    @Override
    public String generateVersion() {
        return Base64.getEncoder().encodeToString(
                String.valueOf(System.currentTimeMillis()).getBytes()
        );
    }
    
    @Override
    public void validatePublish(Video video) {
        if (video == null) {
            throw new IllegalArgumentException("Video cannot be null");
        }
        
        if (video.getTitle() == null || video.getTitle().isBlank()) {
            throw new IllegalStateException("Video title is required for publishing");
        }
        
        if (video.getVisibility() == null) {
            throw new IllegalStateException("Video visibility is required for publishing");
        }
        
        // Additional validations can be added here
    }
}

