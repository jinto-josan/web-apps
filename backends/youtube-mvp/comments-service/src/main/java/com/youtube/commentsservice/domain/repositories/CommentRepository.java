package com.youtube.commentsservice.domain.repositories;

import com.youtube.commentsservice.domain.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Comment aggregate
 * Implemented by infrastructure layer
 */
public interface CommentRepository {
    
    Comment save(Comment comment);
    
    Optional<Comment> findById(String id);
    
    Page<Comment> findByVideoId(String videoId, Pageable pageable);
    
    Page<Comment> findByVideoIdAndParentIdIsNull(String videoId, Pageable pageable);
    
    Page<Comment> findByParentId(String parentId, Pageable pageable);
    
    List<Comment> findByAuthorId(String authorId);
    
    void deleteById(String id);
    
    boolean existsById(String id);
    
    long countByVideoId(String videoId);
    
    List<Comment> findHotThreadsByVideoId(String videoId, int limit);
}

