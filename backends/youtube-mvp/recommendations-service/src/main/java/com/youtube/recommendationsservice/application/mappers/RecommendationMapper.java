package com.youtube.recommendationsservice.application.mappers;

import com.youtube.recommendationsservice.application.dto.RecommendationResponse;
import com.youtube.recommendationsservice.domain.entities.RecommendedItem;
import com.youtube.recommendationsservice.domain.entities.VideoCandidate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RecommendationMapper {
    
    @Mapping(target = "videoId", source = "videoId.value")
    @Mapping(target = "score", source = "score.value")
    RecommendationResponse.RecommendedVideoDto toDto(RecommendedItem item);
    
    List<RecommendationResponse.RecommendedVideoDto> toDtoList(List<RecommendedItem> items);
    
    @Mapping(target = "videoId", source = "videoId.value")
    @Mapping(target = "score", constant = "0.0")
    @Mapping(target = "reason", constant = "")
    RecommendationResponse.RecommendedVideoDto candidateToDto(VideoCandidate candidate);
    
    List<RecommendationResponse.RecommendedVideoDto> candidateToDtoList(List<VideoCandidate> candidates);
}

