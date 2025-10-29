package com.youtube.commentsservice.domain.entities;

import com.youtube.commentsservice.domain.events.CommentCreatedEvent;
import com.youtube.commentsservice.domain.events.CommentDeletedEvent;
import com.youtube.commentsservice.domain.valueobjects.ReactionCount;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregation root for comments and replies
 */
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class Comment {
    private String id;
    private String videoId;
    private String authorId;
    private String parentId; // null for top-level comments
    private String text;
    private CommentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    
    // Reactions tracking
    private Map<String, ReactionCount> reactions;
    
    // Metadata
    private int replyCount;
    private int totalReactionCount;
    private String etag; // For optimistic locking

    public static Comment create(String videoId, String authorId, String parentId, String text) {
        Comment comment = Comment.builder()
                .id(UUID.randomUUID().toString())
                .videoId(videoId)
                .authorId(authorId)
                .parentId(parentId)
                .text(text)
                .status(CommentStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .reactions(new HashMap<>())
                .replyCount(0)
                .totalReactionCount(0)
                .build();
        
        comment.validate();
        return comment;
    }

    public void addReaction(String type, String userId) {
        if (status != CommentStatus.ACTIVE) {
            throw new IllegalStateException("Cannot react to a " + status + " comment");
        }
        
        reactions.computeIfAbsent(type, k -> new ReactionCount(type, 0, new HashMap<>()))
                .addUserReaction(userId);
        totalReactionCount++;
        updatedAt = Instant.now();
    }

    public void removeReaction(String type, String userId) {
        ReactionCount reactionCount = reactions.get(type);
        if (reactionCount != null) {
            reactionCount.removeUserReaction(userId);
            if (reactionCount.getCount() == 0) {
                reactions.remove(type);
            }
            totalReactionCount--;
            updatedAt = Instant.now();
        }
    }

    public void incrementReplyCount() {
        this.replyCount++;
        updatedAt = Instant.now();
    }

    public void delete() {
        if (this.status == CommentStatus.DELETED) {
            return;
        }
        this.status = CommentStatus.DELETED;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isReply() {
        return parentId != null && !parentId.isEmpty();
    }

    public boolean isActive() {
        return status == CommentStatus.ACTIVE;
    }

    private void validate() {
        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("Video ID is required");
        }
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("Author ID is required");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment text is required");
        }
        if (text.length() > 10000) {
            throw new IllegalArgumentException("Comment text exceeds maximum length");
        }
    }

    public CommentCreatedEvent toCreatedEvent() {
        return CommentCreatedEvent.builder()
                .commentId(this.id)
                .videoId(this.videoId)
                .authorId(this.authorId)
                .parentId(this.parentId)
                .text(this.text)
                .timestamp(this.createdAt)
                .build();
    }

    public CommentDeletedEvent toDeletedEvent() {
        return CommentDeletedEvent.builder()
                .commentId(this.id)
                .videoId(this.videoId)
                .authorId(this.authorId)
                .parentId(this.parentId)
                .timestamp(this.deletedAt)
                .build();
    }

    public void updateETag() {
        this.etag = UUID.randomUUID().toString();
    }
}

