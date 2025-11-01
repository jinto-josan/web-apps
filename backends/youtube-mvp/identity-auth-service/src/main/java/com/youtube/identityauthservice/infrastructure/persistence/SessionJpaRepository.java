package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.infrastructure.persistence.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository interface for SessionEntity.
 * Defines operations for session persistence.
 */
@Repository
public interface SessionJpaRepository extends JpaRepository<SessionEntity, String> {

    Optional<SessionEntity> findByJti(String jti);

    List<SessionEntity> findByUserIdAndRevokedAtIsNull(String userId);
}
