package com.youtube.commentsservice.infrastructure.persistence;

import com.youtube.commentsservice.domain.entities.Comment;
import com.youtube.commentsservice.domain.entities.CommentStatus;
import com.youtube.commentsservice.domain.valueobjects.ReactionCount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDocument {
    
    @Id
    private String id;
    
    private String videoId; // Partition key for Cosmos DB
    private String authorId;
    private String parentId;
    private String text;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    
    // Reactions as nested documents
    private Map<String, ReactionDocument> reactions;
    
    private int replyCount;
    private int totalReactionCount;
    private String etag;
    
    @Version
    private Integer version;
    
    public static CommentDocument fromEntity(Comment entity) {
        Map<String, ReactionDocument> reactionDocs = entity.getReactions() != null ?
                entity.getReactions().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> ReactionDocument.fromValueObject(e.getValue())
                        )) : new HashMap<>();
        
        return CommentDocument.builder()
                .id(entity.getId())
                .videoId(entity.getVideoId())
                .authorId(entity.getAuthorId())
                .parentId(entity.getParentId())
                .text(entity.getText())
                .status(entity.getStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .reactions(reactionDocs)
                .replyCount(entity.getReplyCount())
                .totalReactionCount(entity.getTotalReactionCount())
                .etag(entity.getEtag())
                .build();
    }
    
    public Comment toEntity() {
        Map<String, ReactionCount> reactionVOs = reactions != null ?
                reactions.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().toValueObject()
                        )) : new HashMap<>();
        
        Comment comment = Comment.builder()
                .id(this.id)
                .videoId(this.videoId)
                .authorId(this.authorId)
                .parentId(this.parentId)
                .text(this.text)
                .status(CommentStatus.valueOf(this.status))
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .deletedAt(this.deletedAt)
                .reactions(reactionVOs)
                .replyCount(this.replyCount)
                .totalReactionCount(this.totalReactionCount)
                .etag(this.etag)
                .build();
        
        return comment;
    }
}

