package com.youtube.common.domain.web;

import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.common.domain.services.tracing.TraceProvider;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * ExchangeFilterFunction for WebClient to propagate correlation ID and trace context.
 * 
 * <p>This filter automatically adds X-Correlation-Id and traceparent headers
 * to all outbound HTTP requests made via WebClient.</p>
 * 
 * <p>Usage:</p>
 * <pre>{@code
 * WebClient.builder()
 *     .filter(new CorrelationExchangeFilterFunction(traceProvider))
 *     .build();
 * }</pre>
 */
public class CorrelationExchangeFilterFunction implements ExchangeFilterFunction {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String TRACEPARENT_HEADER = "traceparent";
    
    private final TraceProvider traceProvider;
    
    public CorrelationExchangeFilterFunction(TraceProvider traceProvider) {
        this.traceProvider = traceProvider;
    }
    
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest.Builder builder = ClientRequest.from(request);
        
        // Add correlation ID header
        CorrelationContext.getCorrelationId().ifPresent(correlationId -> {
            builder.header(CORRELATION_ID_HEADER, correlationId);
        });
        
        // Add traceparent header
        String traceparent = traceProvider.getTraceparent();
        if (traceparent != null && !traceparent.isBlank()) {
            builder.header(TRACEPARENT_HEADER, traceparent);
        }
        
        return next.exchange(builder.build());
    }
}

