package com.youtube.analyticstelemetryservice.domain.services;

import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;

import java.util.List;

/**
 * Domain service port for dead letter queue (DLQ) operations.
 * Failed events are sent to Blob Storage for later analysis.
 */
public interface DeadLetterQueue {
    
    /**
     * Send a failed event to DLQ (Blob Storage).
     * @param event the failed event
     * @param errorMessage the error message
     * @param exception the exception that caused the failure
     */
    void sendToDlq(TelemetryEvent event, String errorMessage, Throwable exception);
    
    /**
     * Send a batch of failed events to DLQ.
     * @param events the failed events
     * @param errorMessage the error message
     * @param exception the exception that caused the failure
     */
    void sendBatchToDlq(List<TelemetryEvent> events, String errorMessage, Throwable exception);
}

