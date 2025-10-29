package com.youtube.livestreaming.domain.ports;

import com.youtube.livestreaming.domain.valueobjects.AmsLiveEventReference;
import com.youtube.livestreaming.domain.valueobjects.LiveEventConfiguration;

/**
 * Port for Azure Media Services integration
 */
public interface AmsClient {
    /**
     * Create a new live event in Azure Media Services
     */
    AmsLiveEventReference createLiveEvent(String eventName, LiveEventConfiguration config);
    
    /**
     * Start a live event
     */
    void startLiveEvent(String liveEventName);
    
    /**
     * Stop a live event
     */
    void stopLiveEvent(String liveEventName);
    
    /**
     * Delete a live event
     */
    void deleteLiveEvent(String liveEventName);
    
    /**
     * Get live event status
     */
    AmsLiveEventReference getLiveEventStatus(String liveEventName);
    
    /**
     * Archive the live event to on-demand content
     */
    String archiveLiveEvent(String liveEventName);
}

