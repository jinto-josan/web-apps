package com.youtube.commentsservice.application.mappers;

import com.youtube.commentsservice.application.dto.CommentDto;
import com.youtube.commentsservice.application.dto.ReactionSummaryDto;
import com.youtube.commentsservice.domain.entities.Comment;
import com.youtube.commentsservice.domain.valueobjects.ReactionCount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);
    
    @Mapping(target = "status", expression = "java(comment.getStatus().name())")
    @Mapping(target = "reactions", expression = "java(mapReactions(comment.getReactions()))")
    CommentDto toDto(Comment comment);
    
    default Map<String, ReactionSummaryDto> mapReactions(Map<String, ReactionCount> reactions) {
        if (reactions == null) {
            return null;
        }
        return reactions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> ReactionSummaryDto.builder()
                                .type(entry.getValue().getType())
                                .count(entry.getValue().getCount())
                                .build()
                ));
    }
}

