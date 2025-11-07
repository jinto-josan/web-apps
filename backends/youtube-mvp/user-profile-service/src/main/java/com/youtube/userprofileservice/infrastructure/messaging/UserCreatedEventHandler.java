package com.youtube.userprofileservice.infrastructure.messaging;

import com.youtube.common.domain.core.Clock;
import com.youtube.common.domain.core.DomainEvent;
import com.youtube.common.domain.core.UnitOfWork;
import com.youtube.common.domain.events.EventRouter;
import com.youtube.common.domain.events.UserCreatedEvent;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.userprofileservice.domain.entities.AccountProfile;
import com.youtube.userprofileservice.domain.repositories.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Event handler for UserCreatedEvent from identity-auth-service.
 * Creates a default profile when a new user is created.
 */
@Slf4j
@Component
public class UserCreatedEventHandler implements EventRouter.EventHandler<DomainEvent> {
    
    private final ProfileRepository profileRepository;
    private final Clock clock;
    private final UnitOfWork unitOfWork;
    
    public UserCreatedEventHandler(
            ProfileRepository profileRepository,
            Clock clock,
            UnitOfWork unitOfWork) {
        this.profileRepository = profileRepository;
        this.clock = clock;
        this.unitOfWork = unitOfWork;
    }
    
    @Override
    public void handle(DomainEvent event, String correlationId) {
        if (!(event instanceof UserCreatedEvent)) {
            throw new IllegalArgumentException("Expected UserCreatedEvent but got: " + event.getClass());
        }
        UserCreatedEvent userCreatedEvent = (UserCreatedEvent) event;
        log.info("Handling UserCreatedEvent - userId: {}, email: {}, correlationId: {}", 
                userCreatedEvent.getUserId().asString(), userCreatedEvent.getEmail(), correlationId);
        
        unitOfWork.begin();
        try {
            String accountId = userCreatedEvent.getUserId().asString();
            
            // Check if profile already exists (idempotency)
            if (profileRepository.exists(accountId)) {
                log.info("Profile already exists - accountId: {}, correlationId: {}", accountId, correlationId);
                return;
            }
            
            // Create default profile
            Instant now = clock.now();
            AccountProfile profile = AccountProfile.builder()
                    .accountId(accountId)
                    .displayName(userCreatedEvent.getUsername() != null ? userCreatedEvent.getUsername() : 
                            (userCreatedEvent.getEmail() != null ? userCreatedEvent.getEmail().split("@")[0] : "User"))
                    .version(1)
                    .createdAt(now)
                    .updatedAt(now)
                    .updatedBy(accountId)
                    .etag(generateEtag(1, now))
                    .build();
            
            AccountProfile saved = profileRepository.save(profile);
            
            log.info("Profile created successfully - accountId: {}, correlationId: {}", 
                    saved.getAccountId(), correlationId);
            
        } catch (Exception e) {
            log.error("Failed to handle UserCreatedEvent - userId: {}, correlationId: {}", 
                    userCreatedEvent.getUserId().asString(), correlationId, e);
            unitOfWork.rollback(e);
            throw e;
        }
    }
    
    private String generateEtag(int version, Instant timestamp) {
        return String.format("%d-%d", version, timestamp.toEpochMilli());
    }
}

