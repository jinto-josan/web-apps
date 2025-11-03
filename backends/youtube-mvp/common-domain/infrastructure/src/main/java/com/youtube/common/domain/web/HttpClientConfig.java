package com.youtube.common.domain.web;

import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.common.domain.services.tracing.TraceProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for HTTP clients with correlation ID propagation.
 * 
 * <p>Automatically configures RestTemplate to include
 * correlation ID and traceparent headers in outbound requests.</p>
 * 
 * <p>WebClient configuration is handled separately in WebClientAutoConfiguration
 * to avoid class loading issues when WebFlux is not on the classpath.</p>
 * 
 * <p>This configuration only creates beans if they don't already exist,
 * allowing services to provide their own custom configurations if needed.</p>
 */
@Configuration
public class HttpClientConfig {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String TRACEPARENT_HEADER = "traceparent";
    
    /**
     * Creates a RestTemplate bean with correlation ID interceptor.
     * Only created if no RestTemplate bean already exists.
     * 
     * @param traceProvider the trace provider for getting traceparent
     * @return configured RestTemplate with correlation ID propagation
     */
    @Bean
    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(TraceProvider traceProvider) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add correlation ID interceptor
        List<ClientHttpRequestInterceptor> interceptors = 
            new ArrayList<>(restTemplate.getInterceptors());
        
        interceptors.add((request, body, execution) -> {
            // Add correlation ID header
            CorrelationContext.getCorrelationId().ifPresent(correlationId -> {
                request.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
            });
            
            // Add traceparent header
            String traceparent = traceProvider.getTraceparent();
            if (traceparent != null && !traceparent.isBlank()) {
                request.getHeaders().add(TRACEPARENT_HEADER, traceparent);
            }
            
            return execution.execute(request, body);
        });
        
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}

