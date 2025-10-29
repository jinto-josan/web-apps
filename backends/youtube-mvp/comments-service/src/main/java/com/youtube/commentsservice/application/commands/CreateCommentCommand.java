package com.youtube.commentsservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCommentCommand {
    private String idempotencyKey;
    private String videoId;
    private String authorId;
    private String parentId;
    private String text;
}

