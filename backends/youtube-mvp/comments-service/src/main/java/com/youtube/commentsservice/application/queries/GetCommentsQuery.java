package com.youtube.commentsservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@Builder
@AllArgsConstructor
public class GetCommentsQuery {
    private String videoId;
    private String parentId; // null for top-level comments
    private Pageable pageable;
}

