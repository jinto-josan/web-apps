package com.youtube.identityauthservice.interfaces.rest;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.identityauthservice.application.services.DeviceFlowService;
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
@RequestMapping("/auth/device")
public class DeviceController {

    private final DeviceFlowService deviceFlow;
    private final OidcIdTokenVerifier verifier;
    private final UserRepository userRepo;
    private final SessionRefreshService sessionService;
    private final TokenService tokenService;

    @Value("${app.device.verification-uri}")
    private String verificationUri;

    @Value("${app.access-token-ttl-seconds}")
    private int accessTtl;

    public DeviceController(DeviceFlowService deviceFlow, OidcIdTokenVerifier verifier,
                            UserRepository userRepo, SessionRefreshService sessionService,
                            TokenService tokenService) {
        this.deviceFlow = deviceFlow;
        this.verifier = verifier;
        this.userRepo = userRepo;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    @PostMapping("/start")
    public AuthDtos.DeviceStartResponse start(@Valid @RequestBody AuthDtos.DeviceStartRequest req) {
        var ds = deviceFlow.start(verificationUri,
                Optional.ofNullable(req.clientId()).orElse("public"),
                Optional.ofNullable(req.scope()).orElse("openid profile offline_access"));
        return new AuthDtos.DeviceStartResponse(ds.deviceCode, ds.userCode, ds.verificationUri, ds.expiresInSeconds, ds.intervalSeconds);
    }

    @PostMapping("/poll")
    public Object poll(@Valid @RequestBody AuthDtos.DevicePollRequest req) {
        var result = deviceFlow.poll(req.deviceCode());
        if (result.status == DeviceFlowService.Status.EXPIRED) return Map.of("error", "expired_token");
        if (result.status == DeviceFlowService.Status.AUTHORIZATION_PENDING) return Map.of("error", "authorization_pending");

        // Approved => issue tokens for approvedUserId
        String userId = result.approvedUserId;
        User user = userRepo.findById(userId).orElseThrow();
        var sAndRt = sessionService.createSessionWithRefresh(user.getId(), "device", "", "");
        String scope = "openid profile offline_access";
        String access = tokenService.newAccessToken(user.getId(), sAndRt.session.getId(), scope,
                Map.of("name", user.getDisplayName(), "email", user.getEmail()));
        return new AuthDtos.TokenResponse(access, sAndRt.refreshTokenRaw, "Bearer", accessTtl, scope);
    }

    @PostMapping("/verify")
    public java.util.Map<String, Object> verify(@Valid @RequestBody AuthDtos.DeviceVerifyRequest req) {
        var vi = verifier.verify(req.idToken());
        String email = Optional.ofNullable(vi.email()).orElseThrow(() -> new SecurityException("email claim missing"));
        String name = Optional.ofNullable(vi.name()).orElse(email);


        String norm = email.toLowerCase(Locale.ROOT);
        User user = userRepo.findByNormalizedEmail(norm).orElseGet(() -> {
            User u = new User();
            u.setId(com.github.f4b6a3.ulid.UlidCreator.getUlid().toString());
            u.setEmail(email);
            u.setNormalizedEmail(norm);
            u.setDisplayName(name);
            u.setEmailVerified(true);
            u.setStatus((short)1);
            return u;
        });
        user.setEmail(email);
        user.setDisplayName(name);
        user.setEmailVerified(true);
        user.setUpdatedAt(java.time.Instant.now());
        user = userRepo.save(user);

        boolean ok = deviceFlow.verify(req.userCode(), user.getId());
        return java.util.Map.of("ok", ok);
    }
}