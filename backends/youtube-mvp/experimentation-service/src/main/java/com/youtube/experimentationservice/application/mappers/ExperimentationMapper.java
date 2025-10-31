package com.youtube.experimentationservice.application.mappers;

import com.youtube.experimentationservice.application.dto.ExperimentResponse;
import com.youtube.experimentationservice.application.dto.FeatureFlagResponse;
import com.youtube.experimentationservice.domain.model.Experiment;
import com.youtube.experimentationservice.domain.model.FeatureFlag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExperimentationMapper {
    FeatureFlagResponse toResponse(FeatureFlag flag);

    @Mapping(target = "key", source = "experiment.key")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantName", source = "variant.name")
    @Mapping(target = "configuration", source = "variant.configuration")
    ExperimentResponse toResponse(Experiment experiment, Experiment.Variant variant);
}

