package com.youtube.identityauthservice.interfaces.rest;

import com.youtube.identityauthservice.application.commands.ExchangeTokenCommand;
import com.youtube.identityauthservice.application.commands.RefreshTokenCommand;
import com.youtube.identityauthservice.application.commands.RevokeSessionCommand;
import com.youtube.identityauthservice.application.usecases.AuthUseCase;
import com.youtube.identityauthservice.interfaces.rest.dto.AuthDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final AuthUseCase authUseCase;

    @PostMapping("/exchange")
    public AuthDtos.TokenResponse exchange(@Valid @RequestBody AuthDtos.ExchangeRequest req) {
        ExchangeTokenCommand command = ExchangeTokenCommand.builder()
                .idToken(req.idToken())
                .deviceId(req.deviceId())
                .userAgent(req.userAgent())
                .ip(req.ip())
                .scope(req.scope())
                .build();
        
        var result = authUseCase.exchangeToken(command);
        
        return new AuthDtos.TokenResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn(),
                result.scope()
        );
    }

    @PostMapping("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest req) {
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(req.refreshToken())
                .scope(req.scope())
                .build();
        
        var result = authUseCase.refreshToken(command);
        
        return new AuthDtos.TokenResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn(),
                result.scope()
        );
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@Valid @RequestBody AuthDtos.LogoutRequest req) {
        RevokeSessionCommand command = RevokeSessionCommand.builder()
                .refreshToken(req.refreshToken())
                .build();
        
        authUseCase.revokeSession(command);
        
        return Map.of("ok", true);
    }
}
