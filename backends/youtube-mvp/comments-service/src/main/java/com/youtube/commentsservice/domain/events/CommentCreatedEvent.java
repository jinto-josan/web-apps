package com.youtube.commentsservice.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@EqualsAndHashCode
public class CommentCreatedEvent {
    private String commentId;
    private String videoId;
    private String authorId;
    private String parentId; // null for top-level
    private String text;
    private Instant timestamp;
}

