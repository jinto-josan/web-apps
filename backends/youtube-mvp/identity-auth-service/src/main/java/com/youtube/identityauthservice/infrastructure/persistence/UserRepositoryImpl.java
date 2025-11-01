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
    public Optional<User> findById(String userId) {
        return jpaRepo.findById(userId)
                .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = jpaRepo.findById(user.getId())
                .map(existing -> {
                    // Update existing
                    existing.setEmail(user.getEmail());
                    existing.setNormalizedEmail(user.getNormalizedEmail());
                    existing.setDisplayName(user.getDisplayName());
                    existing.setStatus(user.getStatus());
                    existing.setEmailVerified(user.isEmailVerified());
                    existing.setPasswordHash(user.getPasswordHash());
                    existing.setPasswordAlg(user.getPasswordAlg());
                    existing.setMfaEnabled(user.isMfaEnabled());
                    existing.setTermsVersion(user.getTermsVersion());
                    existing.setTermsAcceptedAt(user.getTermsAcceptedAt());
                    existing.setCreatedAt(user.getCreatedAt());
                    existing.setUpdatedAt(user.getUpdatedAt());
                    existing.setVersion(user.getVersion());
                    return existing;
                })
                .orElseGet(() -> UserEntity.fromDomain(user));
        UserEntity saved = jpaRepo.save(entity);
        return saved.toDomain();
    }

    @Override
    public boolean existsByNormalizedEmail(String normalizedEmail) {
        return jpaRepo.existsByNormalizedEmail(normalizedEmail);
    }
}

