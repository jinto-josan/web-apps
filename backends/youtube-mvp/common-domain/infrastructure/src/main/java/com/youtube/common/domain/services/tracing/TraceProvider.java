package com.youtube.common.domain.services.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Service for managing distributed tracing.
 * Wraps Micrometer Tracing to provide trace context propagation.
 */
@Component
public class TraceProvider {
    
    private final Tracer tracer;
    
    public TraceProvider(Tracer tracer) {
        this.tracer = tracer;
    }
    
    /**
     * Starts a new span for a given operation.
     * 
     * @param operationName the name of the operation
     * @param traceparent optional traceparent header value from incoming request
     * @return the started span
     */
    public Span startSpan(String operationName, String traceparent) {
        if (traceparent != null && !traceparent.isBlank()) {
            // Parse traceparent and continue trace context
            // Format: 00-<trace-id>-<parent-id>-<flags>
            String[] parts = traceparent.split("-");
            if (parts.length >= 3) {
                String traceId = parts[1];
                String parentId = parts[2];
                return tracer.nextSpan()
                    .name(operationName)
                    .tag("trace.parent_id", parentId)
                    .start();
            }
        }
        return tracer.nextSpan().name(operationName).start();
    }
    
    /**
     * Starts a new span for a given operation.
     * 
     * @param operationName the name of the operation
     * @return the started span
     */
    public Span startSpan(String operationName) {
        return tracer.nextSpan().name(operationName).start();
    }
    
    /**
     * Ends the current span.
     * 
     * @param span the span to end
     */
    public void endSpan(Span span) {
        if (span != null) {
            span.end();
        }
    }
    
    /**
     * Ends the current span with an error.
     * 
     * @param span the span to end
     * @param throwable the error that occurred
     */
    public void endSpan(Span span, Throwable throwable) {
        if (span != null) {
            span.error(throwable);
            span.end();
        }
    }
    
    /**
     * Gets the current span.
     * 
     * @return the current span, or empty if none
     */
    public Optional<Span> getCurrentSpan() {
        Span span = tracer.currentSpan();
        return Optional.ofNullable(span);
    }
    
    /**
     * Gets the traceparent header value for the current trace.
     * 
     * @return the traceparent header value
     */
    public String getTraceparent() {
        Span span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            String traceId = span.context().traceId();
            String spanId = span.context().spanId();
            // Format: version-traceId-parentSpanId-flags
            return String.format("00-%s-%s-01", traceId, spanId);
        }
        return null;
    }
    
    /**
     * Gets the current trace ID.
     * 
     * @return the trace ID, or empty if none
     */
    public Optional<String> getTraceId() {
        Span span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return Optional.ofNullable(span.context().traceId());
        }
        return Optional.empty();
    }
    
    /**
     * Gets the current span ID.
     * 
     * @return the span ID, or empty if none
     */
    public Optional<String> getSpanId() {
        Span span = tracer.currentSpan();
        if (span != null && span.context() != null) {
            return Optional.ofNullable(span.context().spanId());
        }
        return Optional.empty();
    }
}

