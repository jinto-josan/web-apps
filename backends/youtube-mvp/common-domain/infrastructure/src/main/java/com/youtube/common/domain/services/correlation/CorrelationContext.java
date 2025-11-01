package com.youtube.common.domain.services.correlation;

import java.util.Optional;

/**
 * Service for managing correlation context across a request.
 * Maintains correlation ID, causation ID, and other tracing information.
 * 
 * <p>Thread-local implementation ensures each request has its own context.</p>
 */
public class CorrelationContext {
    
    private static final ThreadLocal<CorrelationIds> context = new ThreadLocal<>();
    
    /**
     * Sets the correlation context for the current thread.
     * 
     * @param correlationId the correlation ID
     * @param causationId the causation ID (ID of the event that caused this)
     * @param traceId the trace ID for distributed tracing
     * @param spanId the span ID for distributed tracing
     */
    public static void set(String correlationId, String causationId, String traceId, String spanId) {
        context.set(new CorrelationIds(correlationId, causationId, traceId, spanId));
    }
    
    /**
     * Sets the correlation ID, generating a new one if not provided.
     * 
     * @param correlationId the correlation ID, or null to generate one
     */
    public static void setCorrelationId(String correlationId) {
        CorrelationIds current = context.get();
        if (current == null) {
            current = new CorrelationIds(
                correlationId != null ? correlationId : generateId(),
                null, null, null
            );
            context.set(current);
        } else {
            context.set(new CorrelationIds(
                correlationId != null ? correlationId : current.correlationId(),
                current.causationId(),
                current.traceId(),
                current.spanId()
            ));
        }
    }
    
    /**
     * Resolves the correlation ID from headers or generates a new one.
     * 
     * @param correlationIdHeader the value from X-Correlation-Id header, or null
     * @return the correlation ID
     */
    public static String resolveCorrelationId(String correlationIdHeader) {
        if (correlationIdHeader != null && !correlationIdHeader.isBlank()) {
            return correlationIdHeader;
        }
        return getCorrelationId().orElse(generateId());
    }
    
    /**
     * Gets the current correlation ID.
     * 
     * @return the correlation ID, or empty if not set
     */
    public static Optional<String> getCorrelationId() {
        CorrelationIds ids = context.get();
        return ids != null ? Optional.ofNullable(ids.correlationId()) : Optional.empty();
    }
    
    /**
     * Gets the current causation ID.
     * 
     * @return the causation ID, or empty if not set
     */
    public static Optional<String> getCausationId() {
        CorrelationIds ids = context.get();
        return ids != null ? Optional.ofNullable(ids.causationId()) : Optional.empty();
    }
    
    /**
     * Gets the current trace ID.
     * 
     * @return the trace ID, or empty if not set
     */
    public static Optional<String> getTraceId() {
        CorrelationIds ids = context.get();
        return ids != null ? Optional.ofNullable(ids.traceId()) : Optional.empty();
    }
    
    /**
     * Gets the current span ID.
     * 
     * @return the span ID, or empty if not set
     */
    public static Optional<String> getSpanId() {
        CorrelationIds ids = context.get();
        return ids != null ? Optional.ofNullable(ids.spanId()) : Optional.empty();
    }
    
    /**
     * Gets all correlation IDs.
     * 
     * @return the correlation IDs, or empty if not set
     */
    public static Optional<CorrelationIds> getCorrelationIds() {
        return Optional.ofNullable(context.get());
    }
    
    /**
     * Clears the correlation context for the current thread.
     */
    public static void clear() {
        context.remove();
    }
    
    private static String generateId() {
        return com.github.f4b6a3.ulid.UlidCreator.getUlid().toString();
    }
    
    /**
     * Record for holding correlation IDs.
     */
    public record CorrelationIds(
        String correlationId,
        String causationId,
        String traceId,
        String spanId
    ) {}
}

