package com.youtube.userprofileservice.domain.repositories;

import com.youtube.userprofileservice.domain.entities.AccountProfile;

import java.util.Optional;

/**
 * Repository interface for account profiles.
 * Follows the Repository pattern to abstract data access.
 */
public interface ProfileRepository {
    
    /**
     * Finds a profile by account ID.
     * 
     * @param accountId the account ID
     * @return an Optional containing the profile if found
     */
    Optional<AccountProfile> findByAccountId(String accountId);
    
    /**
     * Saves a new profile.
     * 
     * @param profile the profile to save
     * @return the saved profile with generated ID
     */
    AccountProfile save(AccountProfile profile);
    
    /**
     * Updates an existing profile.
     * 
     * @param profile the profile to update
     * @return the updated profile
     * @throws com.youtube.common.domain.ConflictException if version mismatch (ETag conflict)
     */
    AccountProfile update(AccountProfile profile);
    
    /**
     * Checks if a profile exists for the given account ID.
     * 
     * @param accountId the account ID
     * @return true if profile exists
     */
    boolean exists(String accountId);
    
    /**
     * Deletes a profile.
     * 
     * @param accountId the account ID to delete
     */
    void delete(String accountId);
}

