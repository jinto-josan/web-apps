package com.youtube.captionssubtitlesservice.application.usecases;

import com.youtube.captionssubtitlesservice.domain.entities.Caption;
import com.youtube.captionssubtitlesservice.domain.repositories.CaptionRepository;
import com.youtube.captionssubtitlesservice.domain.services.CaptionStorageService;
import com.youtube.captionssubtitlesservice.domain.services.TranslationService;
import com.youtube.captionssubtitlesservice.domain.valueobjects.CaptionFormat;
import com.youtube.captionssubtitlesservice.domain.valueobjects.SourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;

/**
 * Use case for translating captions to another language
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateCaptionUseCase {
    
    private final CaptionRepository captionRepository;
    private final CaptionStorageService captionStorageService;
    private final TranslationService translationService;
    
    @Transactional
    public Caption execute(String sourceCaptionId, String targetLanguageCode) {
        log.info("Translating caption: {} to language: {}", sourceCaptionId, targetLanguageCode);
        
        Caption sourceCaption = captionRepository.findById(sourceCaptionId)
            .orElseThrow(() -> new IllegalArgumentException("Source caption not found"));
        
        // Download source content
        String sourceContent = captionStorageService.downloadCaption(sourceCaption.getBlobUri());
        
        // Translate content
        String translatedContent = translationService.translateFile(
            sourceContent, 
            sourceCaption.getLanguage(),
            com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode.fromCode(targetLanguageCode)
        );
        
        // Create new caption for translation
        Caption translatedCaption = new Caption(
            sourceCaption.getVideoId(),
            com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode.fromCode(targetLanguageCode),
            sourceCaption.getFormat(),
            sourceCaption.getGeneratedBy()
        );
        translatedCaption.setSource(SourceType.TRANSLATED);
        translatedCaption.setTranslatedFromCaptionId(sourceCaptionId);
        
        // Upload translated content
        ByteArrayInputStream contentStream = new ByteArrayInputStream(translatedContent.getBytes());
        String blobUri = captionStorageService.uploadCaption(
            translatedCaption.getVideoId(),
            translatedCaption.getId(),
            sourceCaption.getFormat(),
            contentStream
        );
        
        translatedCaption.markAsCompleted(blobUri);
        return captionRepository.save(translatedCaption);
    }
}
