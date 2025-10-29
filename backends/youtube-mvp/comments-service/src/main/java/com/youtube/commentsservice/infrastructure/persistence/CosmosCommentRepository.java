package com.youtube.commentsservice.infrastructure.persistence;

import com.youtube.commentsservice.domain.entities.Comment;
import com.youtube.commentsservice.domain.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CosmosCommentRepository implements CommentRepository {
    
    private final CosmosCommentDocumentRepository documentRepository;
    
    @Override
    public Comment save(Comment comment) {
        CommentDocument doc = CommentDocument.fromEntity(comment);
        doc = documentRepository.save(doc);
        return doc.toEntity();
    }
    
    @Override
    public Optional<Comment> findById(String id) {
        return documentRepository.findById(id)
                .map(CommentDocument::toEntity);
    }
    
    @Override
    public Page<Comment> findByVideoId(String videoId, Pageable pageable) {
        List<CommentDocument> documents = documentRepository.findByVideoId(videoId, pageable);
        long total = documentRepository.countByVideoId(videoId);
        
        List<Comment> comments = documents.stream()
                .map(CommentDocument::toEntity)
                .collect(Collectors.toList());
        
        return new PageImpl<>(comments, pageable, total);
    }
    
    @Override
    public Page<Comment> findByVideoIdAndParentIdIsNull(String videoId, Pageable pageable) {
        List<CommentDocument> documents = documentRepository.findTopLevelByVideoId(
                videoId, pageable.getPageSize());
        
        List<Comment> comments = documents.stream()
                .map(CommentDocument::toEntity)
                .collect(Collectors.toList());
        
        long total = documentRepository.countTopLevelByVideoId(videoId);
        return new PageImpl<>(comments, pageable, total);
    }
    
    @Override
    public Page<Comment> findByParentId(String parentId, Pageable pageable) {
        List<CommentDocument> documents = documentRepository.findRepliesByParentId(
                parentId, pageable.getPageSize());
        
        List<Comment> comments = documents.stream()
                .map(CommentDocument::toEntity)
                .collect(Collectors.toList());
        
        long total = documentRepository.countByParentId(parentId);
        return new PageImpl<>(comments, pageable, total);
    }
    
    @Override
    public List<Comment> findByAuthorId(String authorId) {
        return documentRepository.findByAuthorId(authorId)
                .stream()
                .map(CommentDocument::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(String id) {
        documentRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(String id) {
        return documentRepository.existsById(id);
    }
    
    @Override
    public long countByVideoId(String videoId) {
        return documentRepository.countByVideoId(videoId);
    }
    
    @Override
    public List<Comment> findHotThreadsByVideoId(String videoId, int limit) {
        return documentRepository.findHotThreadsByVideoId(videoId, limit)
                .stream()
                .map(CommentDocument::toEntity)
                .collect(Collectors.toList());
    }
}
