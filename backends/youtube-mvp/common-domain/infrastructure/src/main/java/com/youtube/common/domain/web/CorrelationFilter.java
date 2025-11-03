package com.youtube.common.domain.web;

import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.common.domain.services.tracing.TraceProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to extract and set correlation IDs from HTTP headers.
 * Also integrates with distributed tracing.
 * 
 * <p>This filter should be registered early in the filter chain to ensure
 * correlation context is available for all downstream processing.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Extracts correlation ID from X-Correlation-Id header or generates new one</li>
 *   <li>Extracts traceparent header for distributed tracing</li>
 *   <li>Sets correlation context in thread-local storage</li>
 *   <li>Adds correlation ID to MDC for logging</li>
 *   <li>Returns correlation ID and traceparent in response headers</li>
 * </ul>
 */
public class CorrelationFilter extends OncePerRequestFilter {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String TRACEPARENT_HEADER = "traceparent";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_TRACE_ID = "traceId";
    
    private final TraceProvider traceProvider;
    
    public CorrelationFilter(TraceProvider traceProvider) {
        this.traceProvider = traceProvider;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract correlation ID from header or generate new one
            String correlationId = CorrelationContext.resolveCorrelationId(
                request.getHeader(CORRELATION_ID_HEADER)
            );
            
            // Extract traceparent for distributed tracing
            String traceparent = request.getHeader(TRACEPARENT_HEADER);
            
            // Start tracing span
            var span = traceProvider.startSpan(
                request.getMethod() + " " + request.getRequestURI(),
                traceparent
            );
            
            try {
                // Set correlation context
                String traceId = traceProvider.getTraceId().orElse(null);
                String spanId = traceProvider.getSpanId().orElse(null);
                CorrelationContext.set(correlationId, null, traceId, spanId);
                
                // Add to MDC for logging
                MDC.put(MDC_CORRELATION_ID, correlationId);
                if (traceId != null) {
                    MDC.put(MDC_TRACE_ID, traceId);
                }
                
                // Add correlation ID to response header
                response.setHeader(CORRELATION_ID_HEADER, correlationId);
                
                // Add traceparent to response header
                String responseTraceparent = traceProvider.getTraceparent();
                if (responseTraceparent != null) {
                    response.setHeader(TRACEPARENT_HEADER, responseTraceparent);
                }
                
                filterChain.doFilter(request, response);
                
            } finally {
                // End tracing span
                traceProvider.endSpan(span);
                
                // Clear MDC
                MDC.clear();
                
                // Clear correlation context
                CorrelationContext.clear();
            }
            
        } catch (Exception e) {
            filterChain.doFilter(request, response);
        }
    }
}

