package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Defines operations for user persistence.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByNormalizedEmail(String normalizedEmail);
}
