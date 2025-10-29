package com.youtube.livestreaming.infrastructure.persistence;

import com.youtube.livestreaming.domain.entities.LiveEvent;
import com.youtube.livestreaming.domain.valueobjects.*;
import com.youtube.livestreaming.infrastructure.persistence.entity.LiveEventEntity;
import org.springframework.stereotype.Component;

@Component
public class LiveEventEntityMapper {
    
    public LiveEvent toDomain(LiveEventEntity entity) {
        var config = LiveEventConfiguration.builder()
            .name(entity.getName())
            .description(entity.getDescription())
            .channelId(entity.getChannelId())
            .userId(entity.getUserId())
            .region(entity.getRegion())
            .dvrEnabled(entity.getDvrEnabled())
            .dvrWindowInMinutes(entity.getDvrWindowInMinutes())
            .lowLatencyEnabled(entity.getLowLatencyEnabled())
            .build();
        
        var amsReference = entity.getAmsLiveEventId() != null ? 
            AmsLiveEventReference.builder()
                .liveEventId(entity.getAmsLiveEventId())
                .liveEventName(entity.getAmsLiveEventName())
                .build() : null;
        
        var liveEvent = LiveEvent.builder()
            .id(entity.getId())
            .userId(entity.getUserId())
            .channelId(entity.getChannelId())
            .configuration(config)
            .state(LiveEventState.valueOf(entity.getState()))
            .amsReference(amsReference)
            .ingestUrl(entity.getIngestUrl())
            .previewUrl(entity.getPreviewUrl())
            .createdAt(entity.getCreatedAt())
            .startedAt(entity.getStartedAt())
            .stoppedAt(entity.getStoppedAt())
            .archivedAt(entity.getArchivedAt())
            .failureReason(entity.getFailureReason())
            .build();
        
        return liveEvent;
    }
    
    public LiveEventEntity toEntity(LiveEvent liveEvent) {
        return LiveEventEntity.builder()
            .id(liveEvent.getId())
            .userId(liveEvent.getUserId())
            .channelId(liveEvent.getChannelId())
            .name(liveEvent.getConfiguration().getName())
            .description(liveEvent.getConfiguration().getDescription())
            .state(liveEvent.getState().name())
            .amsLiveEventId(liveEvent.getAmsReference() != null ? liveEvent.getAmsReference().getLiveEventId() : null)
            .amsLiveEventName(liveEvent.getAmsReference() != null ? liveEvent.getAmsReference().getLiveEventName() : null)
            .ingestUrl(liveEvent.getIngestUrl())
            .previewUrl(liveEvent.getPreviewUrl())
            .region(liveEvent.getConfiguration().getRegion())
            .dvrEnabled(liveEvent.getConfiguration().getDvrEnabled())
            .dvrWindowInMinutes(liveEvent.getConfiguration().getDvrWindowInMinutes())
            .lowLatencyEnabled(liveEvent.getConfiguration().getLowLatencyEnabled())
            .createdAt(liveEvent.getCreatedAt())
            .startedAt(liveEvent.getStartedAt())
            .stoppedAt(liveEvent.getStoppedAt())
            .archivedAt(liveEvent.getArchivedAt())
            .failureReason(liveEvent.getFailureReason())
            .build();
    }
}

