package com.youtube.commentsservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeleteCommentCommand {
    private String commentId;
    private String actorId; // user deleting the comment
}

