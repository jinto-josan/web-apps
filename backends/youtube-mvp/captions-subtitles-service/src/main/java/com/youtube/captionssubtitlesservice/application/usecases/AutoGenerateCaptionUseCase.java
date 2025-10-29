package com.youtube.captionssubtitlesservice.application.usecases;

import com.youtube.captionssubtitlesservice.domain.entities.Caption;
import com.youtube.captionssubtitlesservice.domain.repositories.CaptionRepository;
import com.youtube.captionssubtitlesservice.domain.services.CaptionStorageService;
import com.youtube.captionssubtitlesservice.domain.services.SpeechToTextService;
import com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionFormat;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import com.youtube.captionssubtitlesservice.domain.valueobjects.SourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;

/**
 * Use case for auto-generating captions using STT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoGenerateCaptionUseCase {
    
    private final CaptionRepository captionRepository;
    private final CaptionStorageService captionStorageService;
    private final SpeechToTextService speechToTextService;
    
    @Transactional
    public Caption execute(String videoId, String audioUri, LanguageCode language) {
        log.info("Auto-generating caption for videoId: {}, language: {}", videoId, language);
        
        Caption caption = new Caption(videoId, language, CaptionFormat.WebVTT, "system");
        caption.setSource(SourceType.AUTO);
        caption.markAsProcessing();
        caption = captionRepository.save(caption);
        
        try {
            // Generate captions using STT
            String captionContent = speechToTextService.generateCaptions(videoId, audioUri, language);
            
            // Upload to blob storage
            ByteArrayInputStream contentStream = new ByteArrayInputStream(captionContent.getBytes());
            String blobUri = captionStorageService.uploadCaption(videoId, caption.getId(), CaptionFormat.WebVTT, contentStream);
            
            caption.markAsCompleted(blobUri);
            caption.setConfidenceScore(0.95f); // Placeholder - should come from STT service
            
            return captionRepository.save(caption);
            
        } catch (Exception e) {
            log.error("Failed to auto-generate caption", e);
            caption.markAsFailed();
            captionRepository.save(caption);
            throw new RuntimeException("Failed to generate captions", e);
        }
    }
}
