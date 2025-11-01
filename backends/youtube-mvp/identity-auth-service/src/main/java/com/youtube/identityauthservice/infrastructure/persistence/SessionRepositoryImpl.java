package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.domain.entities.Session;
import com.youtube.identityauthservice.domain.repositories.SessionRepository;
import com.youtube.identityauthservice.domain.valueobjects.SessionId;
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
    public Optional<Session> findById(SessionId sessionId) {
        return jpaRepo.findById(sessionId.asString())
                .map(SessionEntity::toDomain);
    }

    @Override
    public Session save(Session session) {
        SessionEntity entity = jpaRepo.findById(session.getId().asString())
                .map(existing -> {
                    existing.setUserId(session.getUserId().asString());
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
    public List<Session> findByUserId(UserId userId) {
        return jpaRepo.findByUserIdAndRevokedAtIsNull(userId.asString()).stream()
                .map(SessionEntity::toDomain)
                .collect(Collectors.toList());
    }
}

