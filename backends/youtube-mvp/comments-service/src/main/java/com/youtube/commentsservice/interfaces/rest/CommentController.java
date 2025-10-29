package com.youtube.commentsservice.interfaces.rest;

import com.youtube.commentsservice.application.commands.AddReactionCommand;
import com.youtube.commentsservice.application.commands.CreateCommentCommand;
import com.youtube.commentsservice.application.commands.DeleteCommentCommand;
import com.youtube.commentsservice.application.dto.CommentDto;
import com.youtube.commentsservice.application.services.CommentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/videos/{videoId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comments and Community API")
public class CommentController {
    
    private final CommentApplicationService commentApplicationService;
    
    @PostMapping
    @Operation(summary = "Create a comment", description = "Create a new comment or reply")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable String videoId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody CreateCommentRequest request) {
        
        String authorId = authentication.getName();
        String effectiveKey = idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString();
        
        CreateCommentCommand command = CreateCommentCommand.builder()
                .idempotencyKey(effectiveKey)
                .videoId(videoId)
                .authorId(authorId)
                .parentId(request.getParentId())
                .text(request.getText())
                .build();
        
        CommentDto comment = commentApplicationService.createComment(command);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.ETAG, comment.getEtag())
                .body(comment);
    }
    
    @GetMapping
    @Operation(summary = "List comments", description = "List comments for a video with pagination")
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable String videoId,
            @RequestParam(required = false) String parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageable = PageRequest.of(page, size);
        Page<CommentDto> comments = commentApplicationService.getComments(
                videoId, parentId, pageable);
        
        return ResponseEntity.ok(comments);
    }
    
    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment", description = "Delete a comment (author only)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @Parameter(hidden = true) Authentication authentication) {
        
        DeleteCommentCommand command = DeleteCommentCommand.builder()
                .commentId(commentId)
                .actorId(authentication.getName())
                .build();
        
        commentApplicationService.deleteComment(command);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{commentId}/reactions")
    @Operation(summary = "Add a reaction", description = "Add a reaction to a comment")
    public ResponseEntity<Void> addReaction(
            @PathVariable String commentId,
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody ReactionRequest request) {
        
        AddReactionCommand command = AddReactionCommand.builder()
                .commentId(commentId)
                .userId(authentication.getName())
                .reactionType(request.getType())
                .build();
        
        commentApplicationService.addReaction(command);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{commentId}/reactions/{type}")
    @Operation(summary = "Remove a reaction", description = "Remove a reaction from a comment")
    public ResponseEntity<Void> removeReaction(
            @PathVariable String commentId,
            @PathVariable String type,
            @Parameter(hidden = true) Authentication authentication) {
        
        commentApplicationService.removeReaction(commentId, authentication.getName(), type);
        return ResponseEntity.noContent().build();
    }
}

@lombok.Data
class CreateCommentRequest {
    @Size(max = 10000, message = "Comment text must not exceed 10000 characters")
    private String text;
    
    private String parentId;
}

@lombok.Data
class ReactionRequest {
    @NotBlank(message = "Reaction type is required")
    private String type;
}

