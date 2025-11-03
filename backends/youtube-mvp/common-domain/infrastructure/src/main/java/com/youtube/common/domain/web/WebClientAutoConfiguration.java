package com.youtube.common.domain.web;

import com.youtube.common.domain.services.tracing.TraceProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configuration for WebClient with correlation ID propagation.
 * 
 * <p>This configuration is separate from HttpClientConfig to avoid
 * class loading issues when Spring WebFlux is not on the classpath.
 * The entire class is only loaded if WebClient is available.</p>
 * 
 * <p>Automatically configures WebClient.Builder to include
 * correlation ID and traceparent headers in outbound requests.</p>
 */
@Configuration
@ConditionalOnClass(WebClient.class)
public class WebClientAutoConfiguration {
    
    /**
     * Creates a WebClient.Builder bean with correlation ID filter function.
     * Only created if WebClient is on classpath and no builder exists.
     * 
     * @param traceProvider the trace provider for getting traceparent
     * @return configured WebClient.Builder with correlation ID propagation
     */
    @Bean
    @ConditionalOnMissingBean(name = "webClientBuilder")
    public WebClient.Builder webClientBuilder(TraceProvider traceProvider) {
        return WebClient.builder()
            .filter(new CorrelationExchangeFilterFunction(traceProvider));
    }
}

