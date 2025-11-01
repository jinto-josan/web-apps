package com.youtube.identityauthservice.application.usecases;

import com.github.f4b6a3.ulid.UlidCreator;
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
    private final SessionRefreshService sessionService;
    private final TokenService tokenService;
    private final EventPublisher eventPublisher;
    private final int accessTtl;

    public AuthUseCaseImpl(
            OidcIdTokenVerifier verifier,
            UserRepository userRepository,
            SessionRefreshService sessionService,
            TokenService tokenService,
            EventPublisher eventPublisher,
            @Value("${app.access-token-ttl-seconds}") int accessTtl) {
        this.verifier = verifier;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
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
        if (isNewUser) {
            // Create new user
            user = User.builder()
                    .id(UlidCreator.getUlid().toString())
                    .email(email)
                    .normalizedEmail(normalizedEmail)
                    .displayName(name)
                    .emailVerified(emailVerified)
                    .status((short) 1)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .version(0)
                    .build();
        } else {
            // Update existing user
            user = existingUserOpt.get();
            user = user.toBuilder()
                    .email(email)
                    .displayName(name)
                    .emailVerified(emailVerified)
                    .updatedAt(Instant.now())
                    .build();
        }

        user = userRepository.save(user);

        // Publish user_created event if this is a new user
        if (isNewUser) {
            UserCreated event = new UserCreated(
                    user.getId(),
                    user.getEmail(),
                    user.getNormalizedEmail(),
                    user.getDisplayName(),
                    user.isEmailVerified(),
                    user.getStatus(),
                    user.getCreatedAt()
            );
            eventPublisher.publishUserCreated(event);
        } else {
            // Optionally publish UserUpdated event
            // eventPublisher.publishUserUpdated(new UserUpdated(...));
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
                user.getId(),
                sessionResult.session.getId(),
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
        var rotated = sessionService.rotateRefreshOrThrow(command.getRefreshToken());
        User user = userRepository.findById(rotated.session.getUserId())
                .orElseThrow(() -> new SecurityException("User not found"));

        String defaultScope = "openid profile offline_access";
        String scope = Optional.ofNullable(command.getScope()).orElse(defaultScope);
        String accessToken = tokenService.newAccessToken(
                user.getId(),
                rotated.session.getId(),
                scope,
                Map.of("name", user.getDisplayName(), "email", user.getEmail())
        );

        return new TokenResponse(accessToken, rotated.refreshTokenRaw, "Bearer", accessTtl, scope);
    }

    @Override
    @Transactional
    public void revokeSession(RevokeSessionCommand command) {
        sessionService.revokeByRawRefreshToken(command.getRefreshToken());
    }

    @Override
    public User getUser(GetUserQuery query) {
        return userRepository.findById(query.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + query.getUserId()));
    }
}

