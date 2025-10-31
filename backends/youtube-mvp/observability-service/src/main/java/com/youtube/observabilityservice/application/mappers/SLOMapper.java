package com.youtube.observabilityservice.application.mappers;

import com.youtube.observabilityservice.application.dto.*;
import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.entities.SLI;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SLOMapper {
    
    SLOResponse toResponse(SLO slo, Double currentSLO, Double errorBudgetBurnRate);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "errorBudget", ignore = true)
    @Mapping(target = "errorBudgetRemaining", ignore = true)
    SLO toDomain(CreateSLORequest request);
    
    SLIResponse toSLIResponse(SLI sli);
    
    List<SLIResponse> toSLIResponseList(List<SLI> slis);
}

