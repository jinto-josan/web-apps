package com.youtube.identityauthservice.interfaces.rest;

import com.youtube.identityauthservice.application.commands.ExchangeTokenCommand;
import com.youtube.identityauthservice.application.commands.RefreshTokenCommand;
import com.youtube.identityauthservice.application.commands.RevokeSessionCommand;
import com.youtube.identityauthservice.application.usecases.AuthUseCase;
import com.youtube.identityauthservice.interfaces.rest.dto.AuthDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication operations.
 * Follows clean architecture by delegating to use cases.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthUseCase authUseCase;

    @PostMapping("/exchange")
    public AuthDtos.TokenResponse exchange(@Valid @RequestBody AuthDtos.ExchangeRequest req) {
        log.debug("Received token exchange request - deviceId: {}, userAgent: {}, ip: {}, scope: {}", 
                req.deviceId(), maskSensitive(req.userAgent()), maskIp(req.ip()), req.scope());
        
        try {
            ExchangeTokenCommand command = ExchangeTokenCommand.builder()
                    .idToken(req.idToken())
                    .deviceId(req.deviceId())
                    .userAgent(req.userAgent())
                    .ip(req.ip())
                    .scope(req.scope())
                    .build();
            
            var result = authUseCase.exchangeToken(command);
            
            log.info("Token exchange successful - expiresIn: {}, scope: {}", result.expiresIn(), result.scope());
            
            return new AuthDtos.TokenResponse(
                    result.accessToken(),
                    result.refreshToken(),
                    result.tokenType(),
                    result.expiresIn(),
                    result.scope()
            );
        } catch (Exception e) {
            log.error("Token exchange failed - deviceId: {}, ip: {}", req.deviceId(), maskIp(req.ip()), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest req) {
        log.debug("Received token refresh request - scope: {}", req.scope());
        
        try {
            RefreshTokenCommand command = RefreshTokenCommand.builder()
                    .refreshToken(req.refreshToken())
                    .scope(req.scope())
                    .build();
            
            var result = authUseCase.refreshToken(command);
            
            log.info("Token refresh successful - expiresIn: {}, scope: {}", result.expiresIn(), result.scope());
            
            return new AuthDtos.TokenResponse(
                    result.accessToken(),
                    result.refreshToken(),
                    result.tokenType(),
                    result.expiresIn(),
                    result.scope()
            );
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw e;
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@Valid @RequestBody AuthDtos.LogoutRequest req) {
        log.debug("Received logout request");
        
        try {
            RevokeSessionCommand command = RevokeSessionCommand.builder()
                    .refreshToken(req.refreshToken())
                    .build();
            
            authUseCase.revokeSession(command);
            
            log.info("Logout successful");
            return Map.of("ok", true);
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw e;
        }
    }
    
    private String maskIp(String ip) {
        if (ip == null) return null;
        // Mask last octet for privacy
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot + 1) + "xxx";
        }
        return ip;
    }
    
    private String maskSensitive(String value) {
        if (value == null) return null;
        if (value.length() > 50) {
            return value.substring(0, 50) + "...";
        }
        return value;
    }
}
