package com.youtube.mvp.videocatalog.infrastructure.persistence.cosmos;

import com.youtube.mvp.videocatalog.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between Video domain and Cosmos entity.
 */
@Mapper(componentModel = "spring")
public abstract class VideoCosmosMapper {
    
    @Mapping(target = "partitionKey", source = "channelId")
    @Mapping(target = "durationSeconds", source = "duration.seconds")
    public abstract VideoCosmosEntity toEntity(Video domain);
    
    @Mapping(target = "duration", expression = "java(toDuration(entity.getDurationSeconds()))")
    @Mapping(target = "state", expression = "java(parseState(entity.getState()))")
    @Mapping(target = "visibility", expression = "java(parseVisibility(entity.getVisibility()))")
    public abstract Video toDomain(VideoCosmosEntity entity);
    
    // Helper methods
    
    protected List<LocalizedText> toDomainList(List<VideoCosmosEntity.LocalizedTextEmbedded> embedded) {
        if (embedded == null) {
            return new ArrayList<>();
        }
        return embedded.stream()
                .map(e -> LocalizedText.builder()
                        .language(e.getLanguage())
                        .text(e.getText())
                        .build())
                .collect(Collectors.toList());
    }
    
    protected List<VideoCosmosEntity.LocalizedTextEmbedded> toEmbeddedList(List<LocalizedText> localized) {
        if (localized == null) {
            return new ArrayList<>();
        }
        return localized.stream()
                .map(lt -> VideoCosmosEntity.LocalizedTextEmbedded.builder()
                        .language(lt.getLanguage())
                        .text(lt.getText())
                        .build())
                .collect(Collectors.toList());
    }
    
    protected Duration toDuration(Long seconds) {
        if (seconds == null) {
            return null;
        }
        return Duration.fromSeconds(seconds);
    }
    
    protected VideoState parseState(String state) {
        if (state == null) {
            return null;
        }
        try {
            return VideoState.valueOf(state);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    protected VideoVisibility parseVisibility(String visibility) {
        if (visibility == null) {
            return null;
        }
        try {
            return VideoVisibility.valueOf(visibility);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

