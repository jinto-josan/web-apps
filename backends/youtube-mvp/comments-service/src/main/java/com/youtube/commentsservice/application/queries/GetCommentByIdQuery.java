package com.youtube.commentsservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetCommentByIdQuery {
    private String commentId;
}

