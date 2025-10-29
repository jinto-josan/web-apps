package com.youtube.commentsservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AddReactionCommand {
    private String commentId;
    private String userId;
    private String reactionType;
}

