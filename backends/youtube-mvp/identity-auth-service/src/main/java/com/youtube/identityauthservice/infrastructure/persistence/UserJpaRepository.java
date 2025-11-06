package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository interface for UserEntity.
 * Defines operations for user persistence.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByNormalizedEmail(String normalizedEmail);
    Optional<UserEntity> findByServicePrincipalId(String servicePrincipalId);
    boolean existsByNormalizedEmail(String normalizedEmail);
}

