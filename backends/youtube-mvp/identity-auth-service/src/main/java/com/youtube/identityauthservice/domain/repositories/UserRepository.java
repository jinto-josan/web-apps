package com.youtube.identityauthservice.domain.repositories;

import com.youtube.common.domain.repository.Repository;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.domain.entities.User;

import java.util.Optional;

/**
 * Repository interface for user entities.
 * Extends common-domain Repository for aggregate root support.
 */
public interface UserRepository extends Repository<User, UserId> {
    
    /**
     * Finds a user by normalized email.
     * 
     * @param normalizedEmail the normalized email
     * @return an Optional containing the user if found
     */
    Optional<User> findByNormalizedEmail(String normalizedEmail);
    
    /**
     * Checks if a user exists for the given normalized email.
     * 
     * @param normalizedEmail the normalized email
     * @return true if user exists
     */
    boolean existsByNormalizedEmail(String normalizedEmail);
}

