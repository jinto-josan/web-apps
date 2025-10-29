package com.youtube.commentsservice.application.services;

import com.youtube.commentsservice.application.commands.CreateCommentCommand;
import com.youtube.commentsservice.application.dto.CommentDto;
import com.youtube.commentsservice.domain.entities.Comment;
import com.youtube.commentsservice.domain.repositories.CommentRepository;
import com.youtube.commentsservice.domain.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentApplicationServiceTest {
    
    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private ProfanityFilterPort profanityFilterPort;
    
    @Mock
    private IdempotencyCheckerPort idempotencyCheckerPort;
    
    @Mock
    private EventPublisherPort eventPublisherPort;
    
    @Mock
    private BroadcastPort broadcastPort;
    
    @InjectMocks
    private CommentApplicationService applicationService;
    
    private Comment testComment;
    
    @BeforeEach
    void setUp() {
        testComment = Comment.builder()
                .id("comment-123")
                .videoId("video-456")
                .authorId("user-789")
                .text("Great video!")
                .status(CommentStatus.ACTIVE)
                .build();
    }
    
    @Test
    void createComment_Success() {
        // Given
        CreateCommentCommand command = CreateCommentCommand.builder()
                .idempotencyKey("key-123")
                .videoId("video-456")
                .authorId("user-789")
                .text("Great video!")
                .build();
        
        when(idempotencyCheckerPort.checkDuplicate("key-123")).thenReturn(Optional.empty());
        when(profanityFilterPort.filterProfanity(any())).thenReturn("Great video!");
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        
        // When
        CommentDto result = applicationService.createComment(command);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("comment-123");
        verify(idempotencyCheckerPort).storeResult("key-123", "comment-123", 3600);
        verify(eventPublisherPort).publishCommentCreated(any());
    }
    
    @Test
    void createComment_Idempotent() {
        // Given
        CreateCommentCommand command = CreateCommentCommand.builder()
                .idempotencyKey("key-123")
                .videoId("video-456")
                .authorId("user-789")
                .text("Great video!")
                .build();
        
        when(idempotencyCheckerPort.checkDuplicate("key-123")).thenReturn(Optional.of("comment-123"));
        when(commentRepository.findById("comment-123")).thenReturn(Optional.of(testComment));
        
        // When
        CommentDto result = applicationService.createComment(command);
        
        // Then
        assertThat(result).isNotNull();
        verify(profanityFilterPort, never()).filterProfanity(any());
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    void createComment_WithProfanity_Filters() {
        // Given
        CreateCommentCommand command = CreateCommentCommand.builder()
                .idempotencyKey("key-123")
                .videoId("video-456")
                .authorId("user-789")
                .text("spam badword")
                .build();
        
        when(idempotencyCheckerPort.checkDuplicate("key-123")).thenReturn(Optional.empty());
        when(profanityFilterPort.filterProfanity("spam badword")).thenReturn("**** ****");
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        
        // When
        applicationService.createComment(command);
        
        // Then
        verify(profanityFilterPort).filterProfanity("spam badword");
    }
}

