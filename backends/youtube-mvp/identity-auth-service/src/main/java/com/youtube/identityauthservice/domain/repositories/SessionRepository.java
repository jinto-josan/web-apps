package com.youtube.identityauthservice.domain.repositories;

import com.youtube.identityauthservice.domain.entities.Session;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for session entities.
 */
public interface SessionRepository {
    
    Optional<Session> findById(String sessionId);
    
    Session save(Session session);
    
    List<Session> findByUserId(String userId);
}

