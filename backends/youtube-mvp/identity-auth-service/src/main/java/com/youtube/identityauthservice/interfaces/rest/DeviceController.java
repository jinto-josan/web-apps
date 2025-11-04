package com.youtube.identityauthservice.interfaces.rest;

import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.application.services.DeviceFlowService;
import com.youtube.identityauthservice.application.services.OidcIdTokenVerifier;
import com.youtube.identityauthservice.application.services.SessionRefreshService;
import com.youtube.identityauthservice.application.services.TokenService;
import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.domain.repositories.UserRepository;
import com.youtube.identityauthservice.infrastructure.persistence.UserRepositoryImpl;
import com.youtube.identityauthservice.interfaces.rest.dto.AuthDtos;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/device")
public class DeviceController {

    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);
    
    private final DeviceFlowService deviceFlow;
    private final OidcIdTokenVerifier verifier;
    private final UserRepository userRepo;
    private final UserRepositoryImpl userRepositoryImpl;
    private final SessionRefreshService sessionService;
    private final TokenService tokenService;

    @Value("${app.device.verification-uri}")
    private String verificationUri;

    @Value("${app.access-token-ttl-seconds}")
    private int accessTtl;

    public DeviceController(DeviceFlowService deviceFlow, OidcIdTokenVerifier verifier,
                            UserRepository userRepo, UserRepositoryImpl userRepositoryImpl,
                            SessionRefreshService sessionService, TokenService tokenService) {
        this.deviceFlow = deviceFlow;
        this.verifier = verifier;
        this.userRepo = userRepo;
        this.userRepositoryImpl = userRepositoryImpl;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    @PostMapping("/start")
    public AuthDtos.DeviceStartResponse start(@Valid @RequestBody AuthDtos.DeviceStartRequest req) {
        log.debug("Device flow start request - clientId: {}, scope: {}", req.clientId(), req.scope());
        
        try {
            var ds = deviceFlow.start(verificationUri,
                    Optional.ofNullable(req.clientId()).orElse("public"),
                    Optional.ofNullable(req.scope()).orElse("openid profile offline_access"));
            
            log.info("Device flow started - deviceCode: {}, expiresIn: {}s", 
                    maskToken(ds.deviceCode), ds.expiresInSeconds);
            
            return new AuthDtos.DeviceStartResponse(ds.deviceCode, ds.userCode, ds.verificationUri, ds.expiresInSeconds, ds.intervalSeconds);
        } catch (Exception e) {
            log.error("Device flow start failed", e);
            throw e;
        }
    }

    @PostMapping("/poll")
    public Object poll(@Valid @RequestBody AuthDtos.DevicePollRequest req) {
        log.debug("Device flow poll request - deviceCode: {}", maskToken(req.deviceCode()));
        
        try {
            var result = deviceFlow.poll(req.deviceCode());
            
            if (result.status == DeviceFlowService.Status.EXPIRED) {
                log.warn("Device flow expired - deviceCode: {}", maskToken(req.deviceCode()));
                return Map.of("error", "expired_token");
            }
            if (result.status == DeviceFlowService.Status.AUTHORIZATION_PENDING) {
                log.debug("Device flow authorization pending - deviceCode: {}", maskToken(req.deviceCode()));
                return Map.of("error", "authorization_pending");
            }

            // Approved => issue tokens for approvedUserId
            log.info("Device flow approved - userId: {}", result.approvedUserId);
            UserId userId = UserId.from(result.approvedUserId);
            User user = userRepo.findById(userId).orElseThrow();
            var sAndRt = sessionService.createSessionWithRefresh(user.getId(), "device", "", "");
            String scope = "openid profile offline_access";
            String access = tokenService.newAccessToken(user.getId().asString(), sAndRt.session.getId().asString(), scope,
                    Map.of("name", user.getDisplayName(), "email", user.getEmail()));
            
            log.info("Device flow tokens issued - userId: {}, sessionId: {}", userId.asString(), sAndRt.session.getId().asString());
            return new AuthDtos.TokenResponse(access, sAndRt.refreshTokenRaw, "Bearer", accessTtl, scope);
        } catch (Exception e) {
            log.error("Device flow poll failed - deviceCode: {}", maskToken(req.deviceCode()), e);
            throw e;
        }
    }

    @PostMapping("/activate")
    public java.util.Map<String, Object> activate(@Valid @RequestBody AuthDtos.DeviceActivateRequest req) {
        log.debug("Device flow activate request - userCode: {}", req.userCode());
        
        try {
            var vi = verifier.verify(req.idToken());
            String email = Optional.ofNullable(vi.email()).orElseThrow(() -> new SecurityException("email claim missing"));
            String name = Optional.ofNullable(vi.name()).orElse(email);

            log.debug("Device flow activation - email: {}, name: {}", email, name);
            String norm = email.toLowerCase(Locale.ROOT);
            User user = userRepo.findByNormalizedEmail(norm).orElseGet(() -> {
                log.info("Creating new user for device flow - email: {}", email);
                return User.builder()
                        .id(UserId.from(com.github.f4b6a3.ulid.UlidCreator.getUlid().toString()))
                        .email(email)
                        .normalizedEmail(norm)
                        .displayName(name)
                        .emailVerified(true)
                        .status((short)1)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .version(0)
                        .build();
            });
            user = user.withEmail(email)
                    .withDisplayName(name)
                    .withEmailVerified(true)
                    .markUpdated();
            user = userRepositoryImpl.saveUser(user);

            boolean ok = deviceFlow.verify(req.userCode(), user.getId().asString());
            log.info("Device flow activation completed - userId: {}, success: {}", user.getId().asString(), ok);
            return java.util.Map.of("ok", ok);
        } catch (Exception e) {
            log.error("Device flow activation failed - userCode: {}", req.userCode(), e);
            throw e;
        }
    }
    
    private String maskToken(String token) {
        if (token == null) return null;
        if (token.length() > 8) {
            return token.substring(0, 8) + "...";
        }
        return token;
    }
}