package com.youtube.captionssubtitlesservice.infrastructure.external;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.models.TranslatedText;
import com.youtube.captionssubtitlesservice.domain.services.TranslationService;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Azure Translator adapter for caption translations
 */
@Slf4j
@Service
public class AzureTranslatorAdapter implements TranslationService {
    
    private final TextTranslationClient translationClient;
    
    public AzureTranslatorAdapter(
            @Value("${azure.translator.endpoint}") String endpoint,
            @Value("${azure.translator.key}") String apiKey) {
        this.translationClient = new TextTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
    }
    
    @Override
    @CircuitBreaker(name = "translator-service")
    @Retry(name = "translator-service")
    @TimeLimiter(name = "translator-service")
    public String translate(String sourceContent, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        log.info("Translating from {} to {}", sourceLanguage, targetLanguage);
        
        var translations = translationClient.translate(
            targetLanguage.getCode(),
            sourceContent
        );
        
        return translations.stream()
            .findFirst()
            .map(TranslatedText::getText)
            .orElse(sourceContent);
    }
    
    @Override
    public String translateFile(String fileContent, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        // Simple implementation - in production, parse SRT/WebVTT and translate timestamps separately
        // For now, translate entire content
        return translate(fileContent, sourceLanguage, targetLanguage);
    }
    
    @Override
    public boolean isAvailable() {
        return translationClient != null;
    }
}
