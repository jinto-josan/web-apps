package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Session entity.
 * Defines operations for session persistence.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    Optional<Session> findByJti(String jti);

    List<Session> findByUserIdAndRevokedAtIsNull(String userId);
}
