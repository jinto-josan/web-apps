package com.youtube.common.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Contains metadata about a domain event.
 * Provides context and tracing information for event processing.
 */
public class EventMetadata {
    private final String aggregateId;
    private final String aggregateType;
    private final String eventType;
    private final int version;
    private final String producer;
    private final String tenantId;
    private final CorrelationIds correlation;
    private final Map<String, String> headers;

    public EventMetadata(String aggregateId, String aggregateType, String eventType, int version,
                        String producer, String tenantId, CorrelationIds correlation, Map<String, String> headers) {
        this.aggregateId = Objects.requireNonNull(aggregateId, "Aggregate ID cannot be null");
        this.aggregateType = Objects.requireNonNull(aggregateType, "Aggregate type cannot be null");
        this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null");
        this.version = version;
        this.producer = Objects.requireNonNull(producer, "Producer cannot be null");
        this.tenantId = tenantId;
        this.correlation = Objects.requireNonNull(correlation, "Correlation IDs cannot be null");
        this.headers = Objects.requireNonNull(headers, "Headers cannot be null");
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public int getVersion() {
        return version;
    }

    public String getProducer() {
        return producer;
    }

    public String getTenantId() {
        return tenantId;
    }

    public CorrelationIds getCorrelation() {
        return correlation;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventMetadata that = (EventMetadata) o;
        return version == that.version &&
               Objects.equals(aggregateId, that.aggregateId) &&
               Objects.equals(aggregateType, that.aggregateType) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(producer, that.producer) &&
               Objects.equals(tenantId, that.tenantId) &&
               Objects.equals(correlation, that.correlation) &&
               Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, aggregateType, eventType, version, producer, tenantId, correlation, headers);
    }

    @Override
    public String toString() {
        return "EventMetadata{aggregateId='" + aggregateId + "', aggregateType='" + aggregateType + 
               "', eventType='" + eventType + "', version=" + version + ", producer='" + producer + 
               "', tenantId='" + tenantId + "', correlation=" + correlation + "}";
    }
}
