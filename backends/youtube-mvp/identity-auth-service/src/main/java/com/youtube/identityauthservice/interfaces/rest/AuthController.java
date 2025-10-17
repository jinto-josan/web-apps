package com.youtube.identityauthservice.interfaces.rest;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.identityauthservice.application.services.OidcIdTokenVerifier;
import com.youtube.identityauthservice.application.services.SessionRefreshService;
import com.youtube.identityauthservice.application.services.TokenService;
import com.youtube.identityauthservice.domain.model.User;
import com.youtube.identityauthservice.infrastructure.persistence.UserRepository;
import com.youtube.identityauthservice.interfaces.rest.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OidcIdTokenVerifier verifier;
    private final UserRepository userRepo;
    private final SessionRefreshService sessionService;
    private final TokenService tokenService;

    @Value("${app.access-token-ttl-seconds}")
    private int accessTtl;

    public AuthController(OidcIdTokenVerifier verifier, UserRepository userRepo,
                          SessionRefreshService sessionService, TokenService tokenService) {
        this.verifier = verifier;
        this.userRepo = userRepo;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    @PostMapping("/exchange")
    public AuthDtos.TokenResponse exchange(@Valid @RequestBody AuthDtos.ExchangeRequest req) {
        var vi = verifier.verify(req.idToken());
        String email = Optional.ofNullable(vi.email()).orElseThrow(() -> new SecurityException("email claim missing"));
        String name = Optional.ofNullable(vi.name()).orElse(email);
        boolean emailVerified = vi.emailVerified();


        String norm = email.toLowerCase(Locale.ROOT);
        User user = userRepo.findByNormalizedEmail(norm).orElseGet(() -> {
            User u = new User();
            u.setId(com.github.f4b6a3.ulid.UlidCreator.getUlid().toString());
            u.setEmail(email);
            u.setNormalizedEmail(norm);
            u.setDisplayName(name);
            u.setEmailVerified(emailVerified);
            u.setStatus((short)1);
            return u;
        });
        user.setEmail(email);
        user.setDisplayName(name);
        user.setEmailVerified(emailVerified);
        user.setUpdatedAt(java.time.Instant.now());
        user = userRepo.save(user);

        var sAndRt = sessionService.createSessionWithRefresh(
                user.getId(),
                Optional.ofNullable(req.deviceId()).orElse("browser"),
                Optional.ofNullable(req.userAgent()).orElse(""),
                Optional.ofNullable(req.ip()).orElse("")
        );
        String scope = Optional.ofNullable(req.scope()).orElse("openid profile offline_access");
        String access = tokenService.newAccessToken(user.getId(), sAndRt.session.getId(), scope,
                java.util.Map.of("name", user.getDisplayName(), "email", user.getEmail()));

        return new AuthDtos.TokenResponse(access, sAndRt.refreshTokenRaw, "Bearer", accessTtl, scope);
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest req) {
        var rotated = sessionService.rotateRefreshOrThrow(req.refreshToken());
        User user = userRepo.findById(rotated.session.getUserId()).orElseThrow();
        String scope = Optional.ofNullable(req.scope()).orElse("openid profile offline_access");
        String access = tokenService.newAccessToken(user.getId(), rotated.session.getId(), scope,
                Map.of("name", user.getDisplayName(), "email", user.getEmail()));
        return new AuthDtos.TokenResponse(access, rotated.refreshTokenRaw, "Bearer", accessTtl, scope);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@Valid @RequestBody AuthDtos.LogoutRequest req) {
        sessionService.revokeByRawRefreshToken(req.refreshToken());
        return Map.of("ok", true);
    }
}
