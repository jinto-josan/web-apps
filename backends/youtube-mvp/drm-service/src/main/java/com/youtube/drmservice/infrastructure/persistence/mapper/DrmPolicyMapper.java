package com.youtube.drmservice.infrastructure.persistence.mapper;

import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.models.KeyRotationPolicy;
import com.youtube.drmservice.domain.models.PolicyConfiguration;
import com.youtube.drmservice.infrastructure.persistence.entity.DrmPolicyEntity;
import com.youtube.drmservice.infrastructure.persistence.entity.KeyRotationPolicyEmbeddable;
import com.youtube.drmservice.infrastructure.persistence.entity.PolicyConfigurationEmbeddable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface DrmPolicyMapper {

    DrmPolicyMapper INSTANCE = Mappers.getMapper(DrmPolicyMapper.class);

    @Mapping(target = "provider", expression = "java(mapProvider(entity.getProvider()))")
    DrmPolicy toDomain(DrmPolicyEntity entity);

    @Mapping(target = "provider", expression = "java(mapProvider(domain.getProvider()))")
    DrmPolicyEntity toEntity(DrmPolicy domain);

    default DrmPolicy.DrmProvider mapProvider(DrmPolicyEntity.DrmProvider provider) {
        return DrmPolicy.DrmProvider.valueOf(provider.name());
    }

    default DrmPolicyEntity.DrmProvider mapProvider(DrmPolicy.DrmProvider provider) {
        return DrmPolicyEntity.DrmProvider.valueOf(provider.name());
    }

    PolicyConfiguration toConfiguration(PolicyConfigurationEmbeddable embeddable);

    PolicyConfigurationEmbeddable toConfigurationEmbeddable(PolicyConfiguration configuration);

    default Map<String, String> mapLicenseConfig(List<Object> list) {
        // Implementation for mapping
        return Map.of();
    }

    default List<String> mapList(List<String> list) {
        return list;
    }

    KeyRotationPolicy toRotationPolicy(KeyRotationPolicyEmbeddable embeddable);

    KeyRotationPolicyEmbeddable toRotationPolicyEmbeddable(KeyRotationPolicy policy);

    default Duration mapDuration(Long seconds) {
        return seconds != null ? Duration.ofSeconds(seconds) : null;
    }

    default Long mapDurationSeconds(Duration duration) {
        return duration != null ? duration.getSeconds() : null;
    }
}

