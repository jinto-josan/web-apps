package com.youtube.userprofileservice.infrastructure.persistence;

import com.youtube.common.domain.error.ConflictException;
import com.youtube.userprofileservice.domain.entities.AccountProfile;
import com.youtube.userprofileservice.domain.repositories.ProfileRepository;
import com.youtube.userprofileservice.infrastructure.persistence.entity.ProfileEntity;
import com.youtube.userprofileservice.infrastructure.persistence.repository.ProfileJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of domain ProfileRepository using JPA.
 */
@Component
@Slf4j
public class ProfileRepositoryImpl implements ProfileRepository {

    private final ProfileJpaRepository jpaRepo;

    public ProfileRepositoryImpl(ProfileJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<AccountProfile> findByAccountId(String accountId) {
        log.debug("Finding profile by account ID - accountId: {}", accountId);
        Optional<AccountProfile> result = jpaRepo.findByAccountId(accountId)
                .map(ProfileEntity::toDomain);
        log.debug("Profile lookup result - accountId: {}, found: {}", accountId, result.isPresent());
        return result;
    }

    @Override
    public AccountProfile save(AccountProfile profile) {
        log.debug("Saving profile - accountId: {}, version: {}", profile.getAccountId(), profile.getVersion());
        try {
            ProfileEntity entity = ProfileEntity.fromDomain(profile);
            ProfileEntity saved = jpaRepo.save(entity);
            AccountProfile savedProfile = saved.toDomain();
            log.debug("Profile saved successfully - accountId: {}, version: {}", 
                    savedProfile.getAccountId(), savedProfile.getVersion());
            return savedProfile;
        } catch (Exception e) {
            log.error("Failed to save profile - accountId: {}", profile.getAccountId(), e);
            throw e;
        }
    }

    @Override
    public AccountProfile update(AccountProfile profile) {
        log.debug("Updating profile - accountId: {}, version: {}", profile.getAccountId(), profile.getVersion());
        try {
            // Check if profile exists
            Optional<ProfileEntity> existingEntityOpt = jpaRepo.findByAccountId(profile.getAccountId());
            if (existingEntityOpt.isEmpty()) {
                log.warn("Profile not found for update - accountId: {}", profile.getAccountId());
                throw new IllegalArgumentException("Profile not found: " + profile.getAccountId());
            }
            
            ProfileEntity existingEntity = existingEntityOpt.get();
            
            // Check version/ETag for optimistic locking
            if (profile.getEtag() != null && !profile.getEtag().equals(existingEntity.getEtag())) {
                log.warn("ETag mismatch detected - accountId: {}, expected: {}, actual: {}", 
                        profile.getAccountId(), profile.getEtag(), existingEntity.getEtag());
                throw new ConflictException("ETag mismatch for profile: " + profile.getAccountId());
            }
            
            // Update existing entity
            ProfileEntity updatedEntity = ProfileEntity.fromDomain(profile);
            updatedEntity.setId(existingEntity.getId()); // Preserve the ID
            
            ProfileEntity saved = jpaRepo.save(updatedEntity);
            AccountProfile updatedProfile = saved.toDomain();
            log.debug("Profile updated successfully - accountId: {}, version: {}", 
                    updatedProfile.getAccountId(), updatedProfile.getVersion());
            return updatedProfile;
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update profile - accountId: {}", profile.getAccountId(), e);
            throw e;
        }
    }

    @Override
    public boolean exists(String accountId) {
        log.debug("Checking if profile exists - accountId: {}", accountId);
        boolean exists = jpaRepo.existsByAccountId(accountId);
        log.debug("Profile exists check result - accountId: {}, exists: {}", accountId, exists);
        return exists;
    }

    @Override
    public void delete(String accountId) {
        log.info("Deleting profile - accountId: {}", accountId);
        try {
            jpaRepo.deleteByAccountId(accountId);
            log.info("Profile deleted successfully - accountId: {}", accountId);
        } catch (Exception e) {
            log.error("Failed to delete profile - accountId: {}", accountId, e);
            throw e;
        }
    }
}

