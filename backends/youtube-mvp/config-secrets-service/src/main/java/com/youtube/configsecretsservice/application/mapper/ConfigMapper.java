package com.youtube.configsecretsservice.application.mapper;

import com.youtube.configsecretsservice.application.dto.ConfigRequest;
import com.youtube.configsecretsservice.application.dto.ConfigResponse;
import com.youtube.configsecretsservice.domain.entity.ConfigurationEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for configuration DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ConfigMapper {
    
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "content-type", source = "contentType")
    ConfigResponse toResponse(ConfigurationEntry entry);
    
    default ConfigurationEntry toEntity(String scope, String key, ConfigRequest request, String userId) {
        if (request == null) {
            return null;
        }
        
        return ConfigurationEntry.builder()
                .scope(scope)
                .key(key)
                .value(request.getValue())
                .contentType(request.getContentType() != null ? request.getContentType() : "text/plain")
                .label(request.getLabel() != null ? request.getLabel() : "")
                .isSecret(request.getIsSecret() != null ? request.getIsSecret() : false)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
    }
}

