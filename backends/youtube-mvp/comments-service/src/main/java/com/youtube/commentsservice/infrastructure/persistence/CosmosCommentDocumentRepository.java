package com.youtube.commentsservice.infrastructure.persistence;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CosmosCommentDocumentRepository extends CosmosRepository<CommentDocument, String> {
    
    @Query(value = "SELECT * FROM c WHERE c.videoId = @videoId AND c.parentId = @parentId")
    Optional<CommentDocument> findByVideoIdAndParentId(@Param("videoId") String videoId, 
                                                        @Param("parentId") String parentId);
    
    @Query(value = "SELECT * FROM c WHERE c.videoId = @videoId AND c.parentId = null ORDER BY c.totalReactionCount DESC OFFSET 0 LIMIT @limit")
    List<CommentDocument> findTopLevelByVideoId(@Param("videoId") String videoId, 
                                                  @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM c WHERE c.parentId = @parentId ORDER BY c.createdAt ASC OFFSET 0 LIMIT @limit")
    List<CommentDocument> findRepliesByParentId(@Param("parentId") String parentId, 
                                                 @Param("limit") int limit);
    
    @Query(value = "SELECT VALUE COUNT(1) FROM c WHERE c.videoId = @videoId")
    long countByVideoId(@Param("videoId") String videoId);
    
    @Query(value = "SELECT * FROM c WHERE c.videoId = @videoId ORDER BY c.totalReactionCount DESC, c.createdAt DESC OFFSET @offset LIMIT @limit")
    List<CommentDocument> findByVideoId(@Param("videoId") String videoId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM c WHERE c.authorId = @authorId")
    List<CommentDocument> findByAuthorId(@Param("authorId") String authorId);
    
    @Query(value = "SELECT VALUE COUNT(1) FROM c WHERE c.videoId = @videoId AND c.parentId = null")
    long countTopLevelByVideoId(@Param("videoId") String videoId);
    
    @Query(value = "SELECT VALUE COUNT(1) FROM c WHERE c.parentId = @parentId")
    long countByParentId(@Param("parentId") String parentId);
    
    @Query(value = "SELECT * FROM c WHERE c.videoId = @videoId AND c.parentId = null ORDER BY c.totalReactionCount DESC OFFSET 0 LIMIT @limit")
    List<CommentDocument> findHotThreadsByVideoId(@Param("videoId") String videoId,
                                                   @Param("limit") int limit);
}

