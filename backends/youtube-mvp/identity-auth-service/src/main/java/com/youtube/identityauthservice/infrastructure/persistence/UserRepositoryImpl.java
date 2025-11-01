package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.domain.repositories.UserRepository;
import com.youtube.identityauthservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of domain UserRepository using JPA.
 */
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepo;

    public UserRepositoryImpl(UserJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<User> findByNormalizedEmail(String normalizedEmail) {
        return jpaRepo.findByNormalizedEmail(normalizedEmail)
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findById(com.youtube.common.domain.shared.valueobjects.UserId userId) {
        return jpaRepo.findById(userId.asString())
                .map(UserEntity::toDomain);
    }

    @Override
    public void save(User aggregate) {
        UserEntity entity = jpaRepo.findById(aggregate.getId().asString())
                .map(existing -> {
                    // Update existing
                    existing.setEmail(aggregate.getEmail());
                    existing.setNormalizedEmail(aggregate.getNormalizedEmail());
                    existing.setDisplayName(aggregate.getDisplayName());
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
                .orElseGet(() -> UserEntity.fromDomain(aggregate));
        jpaRepo.save(entity);
    }

    @Override
    public void save(User aggregate, long expectedVersion) {
        User existing = findById(aggregate.getId()).orElse(null);
        if (existing != null && existing.getVersion() != expectedVersion) {
            throw new com.youtube.common.domain.core.ConcurrencyException(
                "User " + aggregate.getId().asString() + " version mismatch. Expected: " + expectedVersion + ", found: " + existing.getVersion()
            );
        }
        save(aggregate);
    }

    @Override
    public boolean existsByNormalizedEmail(String normalizedEmail) {
        return jpaRepo.existsByNormalizedEmail(normalizedEmail);
    }

    @Override
    public void delete(User aggregate) {
        jpaRepo.deleteById(aggregate.getId().asString());
    }
    
    // Legacy method for backward compatibility - delegates to Repository.save
    public User saveUser(User user) {
        save(user);
        return findById(user.getId()).orElseThrow();
    }
}

