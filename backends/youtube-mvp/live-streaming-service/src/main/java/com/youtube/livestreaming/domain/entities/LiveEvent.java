package com.youtube.livestreaming.domain.entities;

import com.youtube.livestreaming.domain.events.LiveEventCreated;
import com.youtube.livestreaming.domain.events.LiveEventStarted;
import com.youtube.livestreaming.domain.events.LiveEventStopped;
import com.youtube.livestreaming.domain.valueobjects.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Live Event aggregate root
 */
@Getter
@Setter
@Builder
public class LiveEvent {
    private String id;
    private String userId;
    private String channelId;
    private LiveEventConfiguration configuration;
    private LiveEventState state;
    private AmsLiveEventReference amsReference;
    private List<StreamingEndpoint> endpoints;
    private String ingestUrl;
    private String previewUrl;
    private Instant createdAt;
    private Instant startedAt;
    private Instant stoppedAt;
    private Instant archivedAt;
    private String failureReason;
    
    // Domain events
    @Builder.Default
    private List<Object> domainEvents = new ArrayList<>();
    
    public LiveEvent() {
        this.state = LiveEventState.CREATED;
        this.endpoints = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.createdAt = Instant.now();
    }
    
    public LiveEvent(String id, String userId, String channelId, LiveEventConfiguration configuration) {
        this();
        this.id = id;
        this.userId = userId;
        this.channelId = channelId;
        this.configuration = configuration;
    }
    
    public void assignAmsReference(AmsLiveEventReference reference) {
        this.amsReference = reference;
        this.ingestUrl = reference.getIngestUrl();
        this.previewUrl = reference.getPreviewUrl();
    }
    
    public void start() {
        if (!state.canTransitionTo(LiveEventState.STARTING)) {
            throw new IllegalStateException("Cannot start live event from state: " + state);
        }
        
        this.state = LiveEventState.STARTING;
        this.startedAt = Instant.now();
        domainEvents.add(new LiveEventStarted(this.id, this.userId, this.channelId));
    }
    
    public void confirmStarted(String currentState) {
        if (this.state == LiveEventState.STARTING || this.state == LiveEventState.CREATED) {
            this.state = LiveEventState.RUNNING;
            amsReference = AmsLiveEventReference.builder()
                .liveEventId(amsReference.getLiveEventId())
                .liveEventName(amsReference.getLiveEventName())
                .resourceGroupName(amsReference.getResourceGroupName())
                .accountName(amsReference.getAccountName())
                .resourceId(amsReference.getResourceId())
                .ingestUrl(amsReference.getIngestUrl())
                .previewUrl(amsReference.getPreviewUrl())
                .state(currentState)
                .build();
        }
    }
    
    public void stop() {
        if (!state.canTransitionTo(LiveEventState.STOPPING)) {
            throw new IllegalStateException("Cannot stop live event from state: " + state);
        }
        
        this.state = LiveEventState.STOPPING;
        this.stoppedAt = Instant.now();
        domainEvents.add(new LiveEventStopped(this.id, this.userId, this.channelId));
    }
    
    public void confirmStopped() {
        if (this.state == LiveEventState.STOPPING) {
            this.state = LiveEventState.STOPPED;
        }
    }
    
    public void archive() {
        if (!state.canTransitionTo(LiveEventState.ARCHIVING)) {
            throw new IllegalStateException("Cannot archive live event from state: " + state);
        }
        
        this.state = LiveEventState.ARCHIVING;
        this.archivedAt = Instant.now();
    }
    
    public void confirmArchived() {
        if (this.state == LiveEventState.ARCHIVING) {
            this.state = LiveEventState.ARCHIVED;
        }
    }
    
    public void markFailed(String reason) {
        this.state = LiveEventState.FAILED;
        this.failureReason = reason;
    }
    
    public void addEndpoint(StreamingEndpoint endpoint) {
        this.endpoints.add(endpoint);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    public boolean isRunning() {
        return this.state == LiveEventState.RUNNING;
    }
    
    public boolean isActive() {
        return this.state == LiveEventState.STARTING || 
               this.state == LiveEventState.RUNNING || 
               this.state == LiveEventState.STOPPING;
    }
}

