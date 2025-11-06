package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.domain.repositories.UserRepository;
import com.youtube.identityauthservice.infrastructure.persistence.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of domain UserRepository using JPA.
 */
@Component
public class UserRepositoryImpl implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);
    private final UserJpaRepository jpaRepo;

    public UserRepositoryImpl(UserJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<User> findByNormalizedEmail(String normalizedEmail) {
        log.debug("Finding user by normalized email - normalizedEmail: {}", normalizedEmail);
        Optional<User> result = jpaRepo.findByNormalizedEmail(normalizedEmail)
                .map(UserEntity::toDomain);
        log.debug("User lookup result - normalizedEmail: {}, found: {}", normalizedEmail, result.isPresent());
        return result;
    }

    @Override
    public Optional<User> findByServicePrincipalId(String servicePrincipalId) {
        log.debug("Finding user by service principal ID - servicePrincipalId: {}", servicePrincipalId);
        Optional<User> result = jpaRepo.findByServicePrincipalId(servicePrincipalId)
                .map(UserEntity::toDomain);
        log.debug("User lookup result - servicePrincipalId: {}, found: {}", servicePrincipalId, result.isPresent());
        return result;
    }

    @Override
    public Optional<User> findById(com.youtube.common.domain.shared.valueobjects.UserId userId) {
        log.debug("Finding user by ID - userId: {}", userId.asString());
        Optional<User> result = jpaRepo.findById(userId.asString())
                .map(UserEntity::toDomain);
        log.debug("User lookup result - userId: {}, found: {}", userId.asString(), result.isPresent());
        return result;
    }

    @Override
    public void save(User aggregate) {
        log.debug("Saving user - userId: {}, version: {}", aggregate.getId().asString(), aggregate.getVersion());
        try {
            UserEntity entity = jpaRepo.findById(aggregate.getId().asString())
                    .map(existing -> {
                        log.debug("Updating existing user entity - userId: {}", aggregate.getId().asString());
                        // Update existing
                        existing.setEmail(aggregate.getEmail());
                        existing.setNormalizedEmail(aggregate.getNormalizedEmail());
                        existing.setServicePrincipalId(aggregate.getServicePrincipalId());
                        existing.setDisplayName(aggregate.getDisplayName());
                        existing.setUserType(aggregate.getUserType());
                        existing.setStatus(aggregate.getStatus());
                        existing.setEmailVerified(aggregate.isEmailVerified());
                        existing.setPasswordHash(aggregate.getPasswordHash());
                        existing.setPasswordAlg(aggregate.getPasswordAlg());
                        existing.setMfaEnabled(aggregate.isMfaEnabled());
                        existing.setTermsVersion(aggregate.getTermsVersion());
                        existing.setTermsAcceptedAt(aggregate.getTermsAcceptedAt());
                        existing.setCreatedAt(aggregate.getCreatedAt());
                        existing.setUpdatedAt(aggregate.getUpdatedAt());
                        existing.setVersion((int) aggregate.getVersion());
                        return existing;
                    })
                    .orElseGet(() -> {
                        log.debug("Creating new user entity - userId: {}", aggregate.getId().asString());
                        return UserEntity.fromDomain(aggregate);
                    });
            jpaRepo.save(entity);
            log.debug("User saved successfully - userId: {}, version: {}", aggregate.getId().asString(), aggregate.getVersion());
        } catch (Exception e) {
            log.error("Failed to save user - userId: {}", aggregate.getId().asString(), e);
            throw e;
        }
    }

    @Override
    public void save(User aggregate, long expectedVersion) {
        log.debug("Saving user with version check - userId: {}, expectedVersion: {}", 
                aggregate.getId().asString(), expectedVersion);
        User existing = findById(aggregate.getId()).orElse(null);
        if (existing != null && existing.getVersion() != expectedVersion) {
            log.warn("Version mismatch detected - userId: {}, expectedVersion: {}, actualVersion: {}", 
                    aggregate.getId().asString(), expectedVersion, existing.getVersion());
            throw new com.youtube.common.domain.core.ConcurrencyException(
                "User " + aggregate.getId().asString() + " version mismatch. Expected: " + expectedVersion + ", found: " + existing.getVersion()
            );
        }
        save(aggregate);
    }

    @Override
    public boolean existsByNormalizedEmail(String normalizedEmail) {
        log.debug("Checking if user exists by normalized email - normalizedEmail: {}", normalizedEmail);
        boolean exists = jpaRepo.existsByNormalizedEmail(normalizedEmail);
        log.debug("User exists check result - normalizedEmail: {}, exists: {}", normalizedEmail, exists);
        return exists;
    }

    @Override
    public void delete(User aggregate) {
        log.info("Deleting user - userId: {}", aggregate.getId().asString());
        try {
            jpaRepo.deleteById(aggregate.getId().asString());
            log.info("User deleted successfully - userId: {}", aggregate.getId().asString());
        } catch (Exception e) {
            log.error("Failed to delete user - userId: {}", aggregate.getId().asString(), e);
            throw e;
        }
    }
    
    // Legacy method for backward compatibility - delegates to Repository.save
    public User saveUser(User user) {
        log.debug("Saving user (legacy method) - userId: {}", user.getId().asString());
        save(user);
        User saved = findById(user.getId()).orElseThrow(() -> {
            log.error("User not found after save - userId: {}", user.getId().asString());
            return new RuntimeException("User not found after save: " + user.getId().asString());
        });
        log.debug("User saved (legacy method) - userId: {}, version: {}", saved.getId().asString(), saved.getVersion());
        return saved;
    }
}

