package com.youtube.mvp.videocatalog.application.mapper;

import com.youtube.mvp.videocatalog.application.dto.*;
import com.youtube.mvp.videocatalog.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for Video aggregate.
 */
@Mapper(componentModel = "spring")
public interface VideoMapper {
    
    @Mapping(target = "videoId", source = "id")
    Video toDomain(CreateVideoRequest request);
    
    @Mapping(target = "videoId", source = "id")
    VideoResponse toResponse(Video video);
    
    LocalizedText toDomain(LocalizedTextDto dto);
    LocalizedTextDto toDto(LocalizedText localized);
    
    List<LocalizedTextDto> toDtoList(List<LocalizedText> localized);
    List<LocalizedText> toDomainList(List<LocalizedTextDto> dtos);
    
    default DurationDto toDto(Duration duration) {
        if (duration == null) {
            return null;
        }
        return DurationDto.builder()
                .seconds(duration.getSeconds())
                .build();
    }
    
    default Duration toDomain(long seconds) {
        return Duration.fromSeconds(seconds);
    }
}

