package com.youtube.commentsservice.infrastructure.persistence;

import com.youtube.commentsservice.domain.valueobjects.ReactionCount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDocument {
    
    private String type;
    private int count;
    private Map<String, String> userReactions;
    
    public static ReactionDocument fromValueObject(ReactionCount vo) {
        return ReactionDocument.builder()
                .type(vo.getType())
                .count(vo.getCount())
                .userReactions(new HashMap<>(vo.getUserReactions()))
                .build();
    }
    
    public ReactionCount toValueObject() {
        return ReactionCount.builder()
                .type(this.type)
                .count(this.count)
                .userReactions(new HashMap<>(this.userReactions))
                .build();
    }
}

