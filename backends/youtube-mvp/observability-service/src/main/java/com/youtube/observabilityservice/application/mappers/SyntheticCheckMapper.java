package com.youtube.observabilityservice.application.mappers;

import com.youtube.observabilityservice.application.dto.*;
import com.youtube.observabilityservice.domain.entities.SyntheticCheck;
import com.youtube.observabilityservice.domain.entities.SyntheticCheckResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyntheticCheckMapper {
    
    SyntheticCheckResponse toResponse(SyntheticCheck check);
    
    SyntheticCheckResultResponse toResultResponse(SyntheticCheckResult result);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastRunAt", ignore = true)
    @Mapping(target = "lastResult", ignore = true)
    SyntheticCheck toDomain(CreateSyntheticCheckRequest request);
}

