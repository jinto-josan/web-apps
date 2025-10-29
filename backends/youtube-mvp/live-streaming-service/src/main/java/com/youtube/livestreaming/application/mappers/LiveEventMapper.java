package com.youtube.livestreaming.application.mappers;

import com.youtube.livestreaming.application.dtos.*;
import com.youtube.livestreaming.domain.entities.LiveEvent;
import com.youtube.livestreaming.domain.valueobjects.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LiveEventMapper {
    
    @Mapping(target = "domainEvents", ignore = true)
    LiveEvent toEntity(CreateLiveEventRequest request, String userId);
    
    @Mapping(source = "state", target = "state")
    LiveEventResponse toResponse(LiveEvent liveEvent);
    
    List<LiveEventSummaryDto> toSummaryDtos(List<LiveEvent> liveEvents);
    
    LiveEventSummaryDto toSummaryDto(LiveEvent liveEvent);
    
    @Mapping(source = "url", target = "url")
    @Mapping(source = "protocol", target = "protocol")
    @Mapping(source = "resolution", target = "resolution")
    @Mapping(source = "bitrate", target = "bitrate")
    LiveEventResponse.StreamingEndpointDto toEndpointDto(StreamingEndpoint endpoint);
    
    @Mapping(source = "configuration.dvrEnabled", target = "enabled")
    @Mapping(source = "configuration.dvrWindowInMinutes", target = "windowInMinutes")
    LiveEventResponse.DvrInfo toDvrInfo(LiveEvent liveEvent);
    
    default String mapState(LiveEventState state) {
        return state != null ? state.name() : null;
    }
}

