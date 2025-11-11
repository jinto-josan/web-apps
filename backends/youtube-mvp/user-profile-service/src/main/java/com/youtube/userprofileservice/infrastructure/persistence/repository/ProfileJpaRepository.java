package com.youtube.userprofileservice.infrastructure.persistence.repository;

import com.youtube.userprofileservice.infrastructure.persistence.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository interface for ProfileEntity.
 * Defines operations for profile persistence.
 */
@Repository
public interface ProfileJpaRepository extends JpaRepository<ProfileEntity, Long> {
    
    /**
     * Finds a profile by account ID.
     * 
     * @param accountId the account ID
     * @return an Optional containing the profile entity if found
     */
    Optional<ProfileEntity> findByAccountId(String accountId);
    
    /**
     * Checks if a profile exists for the given account ID.
     * 
     * @param accountId the account ID
     * @return true if profile exists
     */
    boolean existsByAccountId(String accountId);
    
    /**
     * Deletes a profile by account ID.
     * 
     * @param accountId the account ID to delete
     */
    void deleteByAccountId(String accountId);
}

