package com.youtube.captionssubtitlesservice.infrastructure.external;

import com.azure.ai.speech.*;
import com.youtube.captionssubtitlesservice.domain.services.SpeechToTextService;
import com.youtube.captionssubtitlesservice.domain.valueobjects.LanguageCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Azure AI Speech adapter for STT operations
 */
@Slf4j
@Service
public class AzureSpeechToTextAdapter implements SpeechToTextService {
    
    private final SpeechConfig speechConfig;
    
    public AzureSpeechToTextAdapter(
            @Value("${azure.speech.key}") String speechKey,
            @Value("${azure.speech.region}") String speechRegion) {
        this.speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
    }
    
    @Override
    @CircuitBreaker(name = "stt-service")
    @Retry(name = "stt-service")
    @TimeLimiter(name = "stt-service")
    public String generateCaptions(String videoId, String audioUri, LanguageCode language) {
        log.info("Generating captions for videoId: {}, language: {}", videoId, language);
        
        speechConfig.setSpeechRecognitionLanguage(language.getCode());
        
        AudioConfig audioConfig = AudioConfig.fromWavFileInput(audioUri);
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);
        
        try {
            var result = recognizer.recognizeOnceAsync().get();
            
            if (result.getReason() == ResultReason.RecognizedSpeech) {
                return result.getText();
            } else {
                throw new RuntimeException("Speech recognition failed: " + result.getReason());
            }
        } catch (Exception e) {
            log.error("Failed to generate captions", e);
            throw new RuntimeException("STT processing failed", e);
        } finally {
            recognizer.close();
            audioConfig.close();
        }
    }
    
    @Override
    public boolean isAvailable() {
        return speechConfig != null;
    }
}
