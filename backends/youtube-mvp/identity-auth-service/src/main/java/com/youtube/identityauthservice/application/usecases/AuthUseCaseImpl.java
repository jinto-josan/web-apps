package com.youtube.identityauthservice.application.usecases;

import com.youtube.common.domain.core.Clock;
import com.youtube.common.domain.core.IdGenerator;
import com.youtube.common.domain.core.UnitOfWork;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.application.commands.ExchangeTokenCommand;
import com.youtube.identityauthservice.application.commands.RefreshTokenCommand;
import com.youtube.identityauthservice.application.commands.RevokeSessionCommand;
import com.youtube.identityauthservice.application.queries.GetUserQuery;
import com.youtube.identityauthservice.application.services.OidcIdTokenVerifier;
import com.youtube.identityauthservice.application.services.SessionRefreshService;
import com.youtube.identityauthservice.application.services.TokenService;
import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.domain.events.UserCreated;
import com.youtube.identityauthservice.domain.repositories.UserRepository;
import com.youtube.identityauthservice.infrastructure.persistence.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Value;
import com.youtube.identityauthservice.domain.services.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of AuthUseCase.
 * Orchestrates authentication operations following clean architecture.
 */
@Service
public class AuthUseCaseImpl implements AuthUseCase {

    private final OidcIdTokenVerifier verifier;
    private final UserRepository userRepository;
    private final UserRepositoryImpl userRepositoryImpl; // For saveUser method
    private final SessionRefreshService sessionService;
    private final TokenService tokenService;
    private final EventPublisher eventPublisher;
    private final IdGenerator<UserId> userIdGenerator;
    private final Clock clock;
    private final UnitOfWork unitOfWork;
    private final int accessTtl;

    public AuthUseCaseImpl(
            OidcIdTokenVerifier verifier,
            UserRepository userRepository,
            UserRepositoryImpl userRepositoryImpl,
            SessionRefreshService sessionService,
            TokenService tokenService,
            EventPublisher eventPublisher,
            IdGenerator<UserId> userIdGenerator,
            Clock clock,
            UnitOfWork unitOfWork,
            @Value("${app.access-token-ttl-seconds}") int accessTtl) {
        this.verifier = verifier;
        this.userRepository = userRepository;
        this.userRepositoryImpl = userRepositoryImpl;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
        this.userIdGenerator = userIdGenerator;
        this.clock = clock;
        this.unitOfWork = unitOfWork;
        this.accessTtl = accessTtl;
    }

    @Override
    @Transactional
    public TokenResponse exchangeToken(ExchangeTokenCommand command) {
        // Verify the ID token
        var verifiedIdentity = verifier.verify(command.getIdToken());
        String email = Optional.ofNullable(verifiedIdentity.email())
                .orElseThrow(() -> new SecurityException("email claim missing"));
        String name = Optional.ofNullable(verifiedIdentity.name()).orElse(email);
        boolean emailVerified = verifiedIdentity.emailVerified();

        // Normalize email for lookup
        String normalizedEmail = email.toLowerCase(Locale.ROOT);

        // Check if user exists, create if not
        Optional<User> existingUserOpt = userRepository.findByNormalizedEmail(normalizedEmail);
        boolean isNewUser = existingUserOpt.isEmpty();

        User user;
        Instant now = clock.now();
        if (isNewUser) {
            // Create new user
            UserId userId = userIdGenerator.nextId();
            user = User.builder()
                    .id(userId)
                    .email(email)
                    .normalizedEmail(normalizedEmail)
                    .displayName(name)
                    .emailVerified(emailVerified)
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(0)
                    .build();
        } else {
            // Update existing user
            user = existingUserOpt.get();
            user = user.withEmail(email)
                    .withDisplayName(name)
                    .withEmailVerified(emailVerified)
                    .markUpdated();
        }

        unitOfWork.begin();
        try {
            userRepository.save(user);
            
            // Publish user_created event if this is a new user
            if (isNewUser) {
                UserCreated event = new UserCreated(
                        user.getId().asString(),
                        user.getEmail(),
                        user.getNormalizedEmail(),
                        user.getDisplayName(),
                        user.isEmailVerified(),
                        user.getStatus(),
                        user.getCreatedAt()
                );
                eventPublisher.publishUserCreated(event);
            }
            
            user = userRepositoryImpl.saveUser(user); // Get saved user with updated version
        } catch (Exception e) {
            unitOfWork.rollback(e);
            throw e;
        }

        // Create session and refresh token
        var sessionResult = sessionService.createSessionWithRefresh(
                user.getId(),
                Optional.ofNullable(command.getDeviceId()).orElse("browser"),
                Optional.ofNullable(command.getUserAgent()).orElse(""),
                Optional.ofNullable(command.getIp()).orElse("")
        );

        // Generate access token
        String defaultScope = "openid profile offline_access";
        String tokenScope = Optional.ofNullable(command.getScope()).orElse(defaultScope);
        String accessToken = tokenService.newAccessToken(
                user.getId().asString(),
                sessionResult.session.getId().asString(),
                tokenScope,
                Map.of("name", user.getDisplayName(), "email", user.getEmail())
        );

        return new TokenResponse(
                accessToken,
                sessionResult.refreshTokenRaw,
                "Bearer",
                accessTtl,
                tokenScope
        );
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenCommand command) {
        unitOfWork.begin();
        try {
            var rotated = sessionService.rotateRefreshOrThrow(command.getRefreshToken());
            UserId userId = UserId.from(rotated.session.getUserId().asString());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new SecurityException("User not found"));

            String defaultScope = "openid profile offline_access";
            String scope = Optional.ofNullable(command.getScope()).orElse(defaultScope);
            String accessToken = tokenService.newAccessToken(
                    user.getId().asString(),
                    rotated.session.getId().asString(),
                    scope,
                    Map.of("name", user.getDisplayName(), "email", user.getEmail())
            );

            return new TokenResponse(accessToken, rotated.refreshTokenRaw, "Bearer", accessTtl, scope);
        } catch (Exception e) {
            unitOfWork.rollback(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void revokeSession(RevokeSessionCommand command) {
        unitOfWork.begin();
        try {
            sessionService.revokeByRawRefreshToken(command.getRefreshToken());
        } catch (Exception e) {
            unitOfWork.rollback(e);
            throw e;
        }
    }

    @Override
    public User getUser(GetUserQuery query) {
        UserId userId = UserId.from(query.getUserId());
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + query.getUserId()));
    }
}

