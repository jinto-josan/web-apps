package com.youtube.moderationservice.infrastructure.adapters.azure;

import com.youtube.moderationservice.application.ports.ContentScannerPort;
import lombok.RequiredArgsConstructor;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AzureContentSafetyAdapter implements ContentScannerPort {

    @Value("${azure.content-safety.endpoint}")
    private String endpoint;
    @Value("${azure.content-safety.key}")
    private String apiKey;

    @Override
    @Retry(name = "contentSafety")
    @CircuitBreaker(name = "contentSafety")
    public Map<String, Double> scanText(String content, Map<String, Object> context) {
        // TODO: Call Azure AI Content Safety REST API; stub scores for now
        Map<String, Double> scores = new HashMap<>();
        scores.put("HATE_SPEECH", content.toLowerCase().contains("hate") ? 0.92 : 0.01);
        scores.put("SEXUAL", 0.05);
        scores.put("VIOLENCE", 0.07);
        return scores;
    }
}


