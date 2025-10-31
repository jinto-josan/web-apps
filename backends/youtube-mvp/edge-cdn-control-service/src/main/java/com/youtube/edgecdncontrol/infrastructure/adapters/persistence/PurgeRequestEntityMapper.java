package com.youtube.edgecdncontrol.infrastructure.adapters.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;
import com.youtube.edgecdncontrol.domain.valueobjects.FrontDoorProfileId;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity.PurgeRequestEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurgeRequestEntityMapper {
    
    private final ObjectMapper objectMapper;
    
    public PurgeRequestEntity toEntity(PurgeRequest request) {
        try {
            return PurgeRequestEntity.builder()
                    .id(request.getId())
                    .resourceGroup(request.getFrontDoorProfile().getResourceGroup())
                    .profileName(request.getFrontDoorProfile().getProfileName())
                    .contentPaths(objectMapper.writeValueAsString(request.getContentPaths()))
                    .purgeType(request.getPurgeType())
                    .requestedBy(request.getRequestedBy())
                    .requestedAt(request.getRequestedAt())
                    .status(request.getStatus())
                    .errorMessage(request.getErrorMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping PurgeRequest to entity", e);
            throw new RuntimeException("Failed to map PurgeRequest to entity", e);
        }
    }
    
    public PurgeRequest toDomain(PurgeRequestEntity entity) {
        try {
            List<String> contentPaths = objectMapper.readValue(
                    entity.getContentPaths(),
                    new TypeReference<List<String>>() {}
            );
            
            return PurgeRequest.builder()
                    .id(entity.getId())
                    .frontDoorProfile(new FrontDoorProfileId(entity.getResourceGroup(), entity.getProfileName()))
                    .contentPaths(contentPaths)
                    .purgeType(entity.getPurgeType())
                    .requestedBy(entity.getRequestedBy())
                    .requestedAt(entity.getRequestedAt())
                    .status(entity.getStatus())
                    .errorMessage(entity.getErrorMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping entity to PurgeRequest", e);
            throw new RuntimeException("Failed to map entity to PurgeRequest", e);
        }
    }
}

