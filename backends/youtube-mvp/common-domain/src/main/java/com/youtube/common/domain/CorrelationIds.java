package com.youtube.common.domain;

import java.util.Objects;

/**
 * Represents correlation IDs for tracing and debugging distributed systems.
 * Contains identifiers for correlation, causation, tracing, and span tracking.
 */
public class CorrelationIds {
    private final String correlationId;
    private final String causationId;
    private final String traceId;
    private final String spanId;

    public CorrelationIds(String correlationId, String causationId, String traceId, String spanId) {
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorrelationIds that = (CorrelationIds) o;
        return Objects.equals(correlationId, that.correlationId) &&
               Objects.equals(causationId, that.causationId) &&
               Objects.equals(traceId, that.traceId) &&
               Objects.equals(spanId, that.spanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, causationId, traceId, spanId);
    }

    @Override
    public String toString() {
        return "CorrelationIds{correlationId='" + correlationId + "', causationId='" + causationId + 
               "', traceId='" + traceId + "', spanId='" + spanId + "'}";
    }
}
