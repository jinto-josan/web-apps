package com.youtube.identityauthservice.domain.repositories;

import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.domain.entities.Session;
import com.youtube.identityauthservice.domain.valueobjects.SessionId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for session entities.
 */
public interface SessionRepository {
    
    Optional<Session> findById(SessionId sessionId);
    
    Session save(Session session);
    
    List<Session> findByUserId(UserId userId);
}

