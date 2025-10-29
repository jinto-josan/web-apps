package com.youtube.captionssubtitlesservice.application.usecases;

import com.youtube.captionssubtitlesservice.domain.entities.Caption;
import com.youtube.captionssubtitlesservice.domain.repositories.CaptionRepository;
import com.youtube.captionssubtitlesservice.domain.services.CaptionStorageService;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case for retrieving captions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetCaptionUseCase {
    
    private final CaptionRepository captionRepository;
    private final CaptionStorageService captionStorageService;
    
    public List<Caption> listByVideoId(String videoId) {
        log.info("Listing captions for videoId: {}", videoId);
        return captionRepository.findByVideoId(videoId);
    }
    
    public Caption getById(String captionId) {
        return captionRepository.findById(captionId)
            .orElseThrow(() -> new IllegalArgumentException("Caption not found"));
    }
    
    public Caption getByVideoAndLanguage(String videoId, LanguageCode language) {
        return captionRepository.findByVideoIdAndLanguage(videoId, language)
            .orElseThrow(() -> new IllegalArgumentException("Caption not found"));
    }
    
    public String getCaptionContent(String blobUri) {
        return captionStorageService.downloadCaption(blobUri);
    }
}
