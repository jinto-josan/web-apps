package com.youtube.commentsservice.application.services;

import com.youtube.commentsservice.application.commands.AddReactionCommand;
import com.youtube.commentsservice.application.commands.CreateCommentCommand;
import com.youtube.commentsservice.application.commands.DeleteCommentCommand;
import com.youtube.commentsservice.application.dto.CommentDto;
import com.youtube.commentsservice.application.mappers.CommentMapper;
import com.youtube.commentsservice.domain.entities.Comment;
import com.youtube.commentsservice.domain.repositories.CommentRepository;
import com.youtube.commentsservice.domain.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentApplicationService {
    
    private final CommentRepository commentRepository;
    private final ProfanityFilterPort profanityFilterPort;
    private final IdempotencyCheckerPort idempotencyCheckerPort;
    private final EventPublisherPort eventPublisherPort;
    private final BroadcastPort broadcastPort;
    private final CommentMapper commentMapper;
    
    public CommentDto createComment(CreateCommentCommand command) {
        log.info("Creating comment for video: {}", command.getVideoId());
        
        // Idempotency check
        Optional<String> existingResult = idempotencyCheckerPort.checkDuplicate(command.getIdempotencyKey());
        if (existingResult.isPresent()) {
            log.info("Duplicate request detected for key: {}", command.getIdempotencyKey());
            return commentRepository.findById(existingResult.get())
                    .map(commentMapper::toDto)
                    .orElseThrow(() -> new IllegalStateException("Idempotent result not found"));
        }
        
        // Profanity filter
        String filteredText = profanityFilterPort.filterProfanity(command.getText());
        if (filteredText != command.getText()) {
            log.warn("Profanity detected and filtered for author: {}", command.getAuthorId());
        }
        
        // Create comment
        Comment comment = Comment.create(
                command.getVideoId(),
                command.getAuthorId(),
                command.getParentId(),
                filteredText
        );
        
        comment.updateETag();
        Comment saved = commentRepository.save(comment);
        
        // Store idempotency result
        idempotencyCheckerPort.storeResult(command.getIdempotencyKey(), saved.getId(), 3600);
        
        // Publish domain event
        eventPublisherPort.publishCommentCreated(comment.toCreatedEvent());
        
        // Broadcast to Web PubSub
        try {
            broadcastPort.broadcastCommentCreated(command.getVideoId(), 
                    jsonFromComment(saved));
        } catch (Exception e) {
            log.error("Failed to broadcast comment creation", e);
        }
        
        return commentMapper.toDto(saved);
    }
    
    public void deleteComment(DeleteCommentCommand command) {
        log.info("Deleting comment: {}", command.getCommentId());
        
        Comment comment = commentRepository.findById(command.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        // Authorization check (can be enhanced with role-based access)
        if (!comment.getAuthorId().equals(command.getActorId())) {
            throw new SecurityException("Unauthorized to delete this comment");
        }
        
        comment.delete();
        commentRepository.save(comment);
        
        // Publish domain event
        eventPublisherPort.publishCommentDeleted(comment.toDeletedEvent());
        
        // Broadcast
        try {
            broadcastPort.broadcastCommentDeleted(comment.getVideoId(), comment.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast comment deletion", e);
        }
    }
    
    public void addReaction(AddReactionCommand command) {
        log.info("Adding reaction {} to comment: {}", command.getReactionType(), command.getCommentId());
        
        Comment comment = commentRepository.findById(command.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        comment.addReaction(command.getReactionType(), command.getUserId());
        comment.updateETag();
        commentRepository.save(comment);
    }
    
    public void removeReaction(String commentId, String userId, String reactionType) {
        log.info("Removing reaction {} from comment: {}", reactionType, commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        
        comment.removeReaction(reactionType, userId);
        comment.updateETag();
        commentRepository.save(comment);
    }
    
    @Transactional(readOnly = true)
    public Page<CommentDto> getComments(String videoId, String parentId, PageRequest pageRequest) {
        log.info("Getting comments for video: {}, parent: {}", videoId, parentId);
        
        Page<Comment> comments;
        if (parentId == null) {
            comments = commentRepository.findByVideoIdAndParentIdIsNull(videoId, pageRequest);
        } else {
            comments = commentRepository.findByParentId(parentId, pageRequest);
        }
        
        return comments.map(commentMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Optional<CommentDto> getCommentById(String commentId) {
        return commentRepository.findById(commentId)
                .map(commentMapper::toDto);
    }
    
    private String jsonFromComment(Comment comment) {
        return String.format(
                "{\"id\":\"%s\",\"videoId\":\"%s\",\"authorId\":\"%s\",\"text\":\"%s\",\"parentId\":\"%s\",\"createdAt\":\"%s\"}",
                comment.getId(), comment.getVideoId(), comment.getAuthorId(), 
                comment.getText(), comment.getParentId(), comment.getCreatedAt()
        );
    }
}

