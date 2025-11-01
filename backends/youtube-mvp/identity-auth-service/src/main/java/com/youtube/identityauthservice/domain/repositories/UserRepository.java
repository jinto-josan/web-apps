package com.youtube.identityauthservice.domain.repositories;

import com.youtube.identityauthservice.domain.entities.User;

import java.util.Optional;

/**
 * Repository interface for user entities.
 * Follows the Repository pattern to abstract data access.
 */
public interface UserRepository {
    
    /**
     * Finds a user by normalized email.
     * 
     * @param normalizedEmail the normalized email
     * @return an Optional containing the user if found
     */
    Optional<User> findByNormalizedEmail(String normalizedEmail);
    
    /**
     * Finds a user by ID.
     * 
     * @param userId the user ID
     * @return an Optional containing the user if found
     */
    Optional<User> findById(String userId);
    
    /**
     * Saves a new user.
     * 
     * @param user the user to save
     * @return the saved user with generated ID
     */
    User save(User user);
    
    /**
     * Checks if a user exists for the given normalized email.
     * 
     * @param normalizedEmail the normalized email
     * @return true if user exists
     */
    boolean existsByNormalizedEmail(String normalizedEmail);
}

