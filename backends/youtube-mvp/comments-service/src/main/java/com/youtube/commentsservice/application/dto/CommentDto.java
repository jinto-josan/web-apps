package com.youtube.commentsservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String id;
    private String videoId;
    private String authorId;
    private String parentId;
    private String text;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Reactions
    private Map<String, ReactionSummaryDto> reactions;
    private int totalReactionCount;
    
    // Metadata
    private int replyCount;
    private String etag;
}

