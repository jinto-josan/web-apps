package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.entities.Session;
import com.youtube.identityauthservice.domain.repositories.SessionRepository;
import com.youtube.identityauthservice.infrastructure.persistence.entity.SessionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of domain SessionRepository using JPA.
 */
@Component
public class SessionRepositoryImpl implements SessionRepository {

    private final SessionJpaRepository jpaRepo;

    public SessionRepositoryImpl(SessionJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<Session> findById(String sessionId) {
        return jpaRepo.findById(sessionId)
                .map(SessionEntity::toDomain);
    }

    @Override
    public Session save(Session session) {
        SessionEntity entity = jpaRepo.findById(session.getId())
                .map(existing -> {
                    existing.setUserId(session.getUserId());
                    existing.setJti(session.getJti());
                    existing.setDeviceId(session.getDeviceId());
                    existing.setUserAgent(session.getUserAgent());
                    existing.setIp(session.getIp());
                    existing.setRevokedAt(session.getRevokedAt());
                    existing.setRevokeReason(session.getRevokeReason());
                    return existing;
                })
                .orElseGet(() -> SessionEntity.fromDomain(session));
        SessionEntity saved = jpaRepo.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<Session> findByUserId(String userId) {
        return jpaRepo.findByUserIdAndRevokedAtIsNull(userId).stream()
                .map(SessionEntity::toDomain)
                .collect(Collectors.toList());
    }
}

