package com.youtube.captionssubtitlesservice.application.usecases;

import com.youtube.captionssubtitlesservice.domain.entities.Caption;
import com.youtube.captionssubtitlesservice.domain.repositories.CaptionRepository;
import com.youtube.captionssubtitlesservice.domain.services.CaptionStorageService;
import com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionFormat;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

/**
 * Use case for uploading and creating captions manually
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCaptionUseCase {
    
    private final CaptionRepository captionRepository;
    private final CaptionStorageService captionStorageService;
    
    @Transactional
    public Caption execute(String videoId, LanguageCode language, CaptionFormat format, 
                          InputStream content, String userId) {
        log.info("Creating caption for videoId: {}, language: {}", videoId, language);
        
        Caption caption = new Caption(videoId, language, format, userId);
        caption = captionRepository.save(caption);
        
        // Upload to blob storage
        String blobUri = captionStorageService.uploadCaption(videoId, caption.getId(), format, content);
        caption.markAsCompleted(blobUri);
        
        return captionRepository.save(caption);
    }
}
