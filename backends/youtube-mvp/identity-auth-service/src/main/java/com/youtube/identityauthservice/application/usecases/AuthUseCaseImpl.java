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
import com.youtube.identityauthservice.domain.entities.UserType;
import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.domain.events.UserCreated;
import com.youtube.identityauthservice.domain.repositories.UserRepository;
import com.youtube.identityauthservice.infrastructure.persistence.UserRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.youtube.identityauthservice.domain.services.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of AuthUseCase.
 * Orchestrates authentication operations following clean architecture.
 */
@Service
public class AuthUseCaseImpl implements AuthUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthUseCaseImpl.class);

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
            @Value("${identity-auth.access-token-ttl-seconds}") int accessTtl) {
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
        log.debug("Processing token exchange command");
        
        // Verify the ID token
        var verifiedIdentity = verifier.verify(command.getIdToken());
        
        User user;
        Instant now = clock.now();
        boolean isNewUser;
        
        if (verifiedIdentity.isServicePrincipal()) {
            // Handle service principal authentication
            String servicePrincipalId = verifiedIdentity.servicePrincipalId();
            if (servicePrincipalId == null || servicePrincipalId.isBlank()) {
                log.error("Token exchange failed: service principal ID missing");
                throw new SecurityException("service principal ID missing");
            }
            
            String displayName = Optional.ofNullable(verifiedIdentity.name())
                    .orElse("Service Principal " + servicePrincipalId.substring(0, Math.min(8, servicePrincipalId.length())));
            
            log.debug("ID token verified - servicePrincipalId: {}, displayName: {}", servicePrincipalId, displayName);
            
            // Check if service principal exists, create if not
            Optional<User> existingUserOpt = userRepository.findByServicePrincipalId(servicePrincipalId);
            isNewUser = existingUserOpt.isEmpty();
            log.debug("Service principal lookup - servicePrincipalId: {}, isNewUser: {}", servicePrincipalId, isNewUser);
            
            if (isNewUser) {
                // Create new service principal user
                UserId userId = userIdGenerator.nextId();
                log.info("Creating new service principal - userId: {}, servicePrincipalId: {}", userId.asString(), servicePrincipalId);
                user = User.builder()
                        .id(userId)
                        .userType(UserType.SERVICE_PRINCIPAL)
                        .servicePrincipalId(servicePrincipalId)
                        .displayName(displayName)
                        .email(null)
                        .normalizedEmail(null)
                        .emailVerified(false)
                        .status((short) 1)
                        .createdAt(now)
                        .updatedAt(now)
                        .version(0)
                        .build();
            } else {
                // Update existing service principal
                user = existingUserOpt.get();
                log.debug("Updating existing service principal - userId: {}", user.getId().asString());
                user = user.withDisplayName(displayName).markUpdated();
            }
        } else {
            // Handle regular user authentication (email-based)
            String email = Optional.ofNullable(verifiedIdentity.email())
                    .orElseThrow(() -> {
                        log.error("Token exchange failed: email claim missing");
                        return new SecurityException("email claim missing");
                    });
            String name = Optional.ofNullable(verifiedIdentity.name()).orElse(email);
            boolean emailVerified = verifiedIdentity.emailVerified();

            log.debug("ID token verified - email: {}, emailVerified: {}", email, emailVerified);

            // Normalize email for lookup
            String normalizedEmail = email.toLowerCase(Locale.ROOT);

            // Check if user exists, create if not
            Optional<User> existingUserOpt = userRepository.findByNormalizedEmail(normalizedEmail);
            isNewUser = existingUserOpt.isEmpty();
            log.debug("User lookup - normalizedEmail: {}, isNewUser: {}", normalizedEmail, isNewUser);

            if (isNewUser) {
                // Create new user
                UserId userId = userIdGenerator.nextId();
                log.info("Creating new user - userId: {}, email: {}", userId.asString(), email);
                user = User.builder()
                        .id(userId)
                        .userType(UserType.USER)
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
                log.debug("Updating existing user - userId: {}", user.getId().asString());
                user = user.withEmail(email)
                        .withDisplayName(name)
                        .withEmailVerified(emailVerified)
                        .markUpdated();
            }
        }

        unitOfWork.begin();
        try {
            userRepository.save(user);
            
            // Publish user_created event if this is a new user
            if (isNewUser) {
                log.debug("Publishing UserCreated event - userId: {}, userType: {}", 
                        user.getId().asString(), user.getUserType());
                UserCreated event = new UserCreated(
                        user.getId().asString(),
                        user.getUserType(),
                        user.getEmail(),
                        user.getNormalizedEmail(),
                        user.getServicePrincipalId(),
                        user.getDisplayName(),
                        user.isEmailVerified(),
                        user.getStatus(),
                        user.getCreatedAt()
                );
                eventPublisher.publishUserCreated(event);
            }
            
            user = userRepositoryImpl.saveUser(user); // Get saved user with updated version
            log.debug("User saved successfully - userId: {}, version: {}", user.getId().asString(), user.getVersion());
        } catch (Exception e) {
            log.error("Failed to save user during token exchange - userId: {}", 
                    user.getId() != null ? user.getId().asString() : "unknown", e);
            unitOfWork.rollback(e);
            throw e;
        }

        // Create session and refresh token
        log.debug("Creating session with refresh token - userId: {}", user.getId().asString());
        var sessionResult = sessionService.createSessionWithRefresh(
                user.getId(),
                Optional.ofNullable(command.getDeviceId()).orElse("browser"),
                Optional.ofNullable(command.getUserAgent()).orElse(""),
                Optional.ofNullable(command.getIp()).orElse("")
        );
        log.debug("Session created - sessionId: {}", sessionResult.session.getId().asString());

        // Generate access token
        String defaultScope = "openid profile offline_access";
        String tokenScope = Optional.ofNullable(command.getScope()).orElse(defaultScope);
        log.debug("Generating access token - userId: {}, sessionId: {}, scope: {}", 
                user.getId().asString(), sessionResult.session.getId().asString(), tokenScope);
        
        // Build token claims based on user type
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("name", user.getDisplayName());
        if (user.getUserType() == UserType.USER && user.getEmail() != null) {
            tokenClaims.put("email", user.getEmail());
        } else if (user.getUserType() == UserType.SERVICE_PRINCIPAL && user.getServicePrincipalId() != null) {
            tokenClaims.put("service_principal_id", user.getServicePrincipalId());
        }
        
        String accessToken = tokenService.newAccessToken(
                user.getId().asString(),
                sessionResult.session.getId().asString(),
                tokenScope,
                tokenClaims
        );

        log.info("Token exchange completed successfully - userId: {}, sessionId: {}, isNewUser: {}, userType: {}", 
                user.getId().asString(), sessionResult.session.getId().asString(), isNewUser, user.getUserType());

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
        log.debug("Processing token refresh command");
        
        unitOfWork.begin();
        try {
            var rotated = sessionService.rotateRefreshOrThrow(command.getRefreshToken());
            log.debug("Refresh token rotated successfully - sessionId: {}", rotated.session.getId().asString());
            
            UserId userId = UserId.from(rotated.session.getUserId().asString());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found during token refresh - userId: {}", userId.asString());
                        return new SecurityException("User not found");
                    });

            String defaultScope = "openid profile offline_access";
            String scope = Optional.ofNullable(command.getScope()).orElse(defaultScope);
            log.debug("Generating access token for refresh - userId: {}, sessionId: {}, scope: {}", 
                    userId.asString(), rotated.session.getId().asString(), scope);
            
            // Build token claims based on user type
            Map<String, Object> tokenClaims = new HashMap<>();
            tokenClaims.put("name", user.getDisplayName());
            if (user.getUserType() == UserType.USER && user.getEmail() != null) {
                tokenClaims.put("email", user.getEmail());
            } else if (user.getUserType() == UserType.SERVICE_PRINCIPAL && user.getServicePrincipalId() != null) {
                tokenClaims.put("service_principal_id", user.getServicePrincipalId());
            }
            
            String accessToken = tokenService.newAccessToken(
                    user.getId().asString(),
                    rotated.session.getId().asString(),
                    scope,
                    tokenClaims
            );

            log.info("Token refresh completed successfully - userId: {}, sessionId: {}", 
                    userId.asString(), rotated.session.getId().asString());
            
            return new TokenResponse(accessToken, rotated.refreshTokenRaw, "Bearer", accessTtl, scope);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            unitOfWork.rollback(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void revokeSession(RevokeSessionCommand command) {
        log.debug("Processing session revocation command");
        
        unitOfWork.begin();
        try {
            sessionService.revokeByRawRefreshToken(command.getRefreshToken());
            log.info("Session revoked successfully");
        } catch (Exception e) {
            log.error("Session revocation failed", e);
            unitOfWork.rollback(e);
            throw e;
        }
    }

    @Override
    public User getUser(GetUserQuery query) {
        log.debug("Getting user - userId: {}", query.getUserId());
        
        UserId userId = UserId.from(query.getUserId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found - userId: {}", query.getUserId());
                    return new IllegalArgumentException("User not found: " + query.getUserId());
                });
        
        log.debug("User retrieved successfully - userId: {}", query.getUserId());
        return user;
    }
}

