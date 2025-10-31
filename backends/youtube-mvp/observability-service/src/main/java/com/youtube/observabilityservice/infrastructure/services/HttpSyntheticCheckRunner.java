package com.youtube.observabilityservice.infrastructure.services;

import com.youtube.observabilityservice.domain.entities.SyntheticCheck;
import com.youtube.observabilityservice.domain.entities.SyntheticCheckResult;
import com.youtube.observabilityservice.domain.services.SyntheticCheckRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpSyntheticCheckRunner implements SyntheticCheckRunner {
    
    private final OkHttpClient httpClient;
    
    @Override
    public SyntheticCheckResult run(SyntheticCheck check) {
        if (check.getType() != SyntheticCheck.SyntheticCheckType.HTTP 
                && check.getType() != SyntheticCheck.SyntheticCheckType.HTTPS) {
            throw new IllegalArgumentException("Unsupported check type: " + check.getType());
        }
        
        long startTime = System.currentTimeMillis();
        boolean success = false;
        Integer statusCode = null;
        String responseBody = null;
        String errorMessage = null;
        Map<String, String> metadata = new HashMap<>();
        
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(check.getEndpoint())
                    .timeout(check.getTimeoutSeconds() != null 
                            ? java.time.Duration.ofSeconds(check.getTimeoutSeconds())
                            : java.time.Duration.ofSeconds(30));
            
            if (check.getMethod().equalsIgnoreCase("POST") && check.getBody() != null) {
                RequestBody body = RequestBody.create(
                        check.getBody(),
                        MediaType.get("application/json; charset=utf-8")
                );
                requestBuilder.method("POST", body);
            }
            
            if (check.getHeaders() != null) {
                for (Map.Entry<String, String> header : check.getHeaders().entrySet()) {
                    requestBuilder.addHeader(header.getKey(), header.getValue());
                }
            }
            
            Request request = requestBuilder.build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                statusCode = response.code();
                if (response.body() != null) {
                    responseBody = response.body().string();
                }
                
                // Check status code
                if (check.getExpectedStatusCode() != null) {
                    success = statusCode.equals(check.getExpectedStatusCode());
                } else {
                    success = statusCode >= 200 && statusCode < 300;
                }
                
                // Check response body pattern if specified
                if (success && check.getExpectedBodyPattern() != null && responseBody != null) {
                    success = responseBody.matches(check.getExpectedBodyPattern());
                    if (!success) {
                        errorMessage = "Response body did not match expected pattern";
                    }
                }
                
                metadata.put("statusCode", String.valueOf(statusCode));
                metadata.put("contentType", response.header("Content-Type"));
            }
            
        } catch (IOException e) {
            errorMessage = e.getMessage();
            log.error("Synthetic check failed for {}: {}", check.getEndpoint(), e.getMessage());
        }
        
        long responseTimeMs = System.currentTimeMillis() - startTime;
        metadata.put("responseTimeMs", String.valueOf(responseTimeMs));
        
        return SyntheticCheckResult.builder()
                .executedAt(Instant.now())
                .success(success)
                .statusCode(statusCode)
                .responseTimeMs(responseTimeMs)
                .responseBody(responseBody)
                .errorMessage(errorMessage)
                .metadata(metadata)
                .build();
    }
}

