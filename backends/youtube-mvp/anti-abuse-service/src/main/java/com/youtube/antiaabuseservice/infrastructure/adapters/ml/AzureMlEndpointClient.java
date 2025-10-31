package com.youtube.antiaabuseservice.infrastructure.adapters.ml;

import com.youtube.antiaabuseservice.domain.services.MlEndpointClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AzureMlEndpointClient implements MlEndpointClient {
    
    @Value("${azure.ml.endpoint-url:}")
    private String endpointUrl;
    
    @Value("${azure.ml.api-key:}")
    private String apiKey;
    
    private final WebClient.Builder webClientBuilder;

    @Override
    @CircuitBreaker(name = "mlEndpoint")
    @Retry(name = "mlEndpoint")
    @TimeLimiter(name = "mlEndpoint")
    public Map<String, Object> predict(Map<String, Object> features) {
        if (endpointUrl == null || endpointUrl.isEmpty()) {
            log.warn("ML endpoint not configured, returning default score");
            return Map.of("risk_score", 0.0);
        }
        
        WebClient webClient = webClientBuilder
                .baseUrl(endpointUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", features);
        
        try {
            return webClient.post()
                    .uri("/score")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(2))
                    .blockOptional()
                    .orElseGet(() -> {
                        log.warn("ML endpoint timeout, returning fallback score");
                        return Map.of("risk_score", 0.0);
                    });
        } catch (Exception e) {
            log.error("Error calling ML endpoint", e);
            return Map.of("risk_score", 0.0);
        }
    }
}

