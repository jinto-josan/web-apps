package com.youtube.antiaabuseservice.application.mappers;

import com.youtube.antiaabuseservice.application.dto.RiskScoreRequest;
import com.youtube.antiaabuseservice.application.dto.RiskScoreResponse;
import com.youtube.antiaabuseservice.domain.model.RiskEvent;
import com.youtube.antiaabuseservice.domain.model.RiskScore;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AntiAbuseMapper {
    RiskScoreResponse toResponse(RiskScore score);
    
    @Mapping(target = "eventType", expression = "java(com.youtube.antiaabuseservice.domain.model.RiskEvent.EventType.valueOf(request.getEventType().toUpperCase()))")
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    RiskEvent toDomain(RiskScoreRequest request);
}

