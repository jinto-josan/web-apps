package com.youtube.identityauthservice.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    public record ExchangeRequest(
            @NotBlank String idToken,
            String deviceId,
            String userAgent,
            String ip,
            String scope
    ) {}

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            String scope
    ) {}

    public record RefreshRequest(@NotBlank String refreshToken, String scope) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}

    public record DeviceStartRequest(String clientId, String scope) {}
    public record DeviceStartResponse(String deviceCode, String userCode, String verificationUri, long expiresIn, long interval) {}
    public record DevicePollRequest(@NotBlank String deviceCode) {}
    public record DeviceVerifyRequest(@NotBlank String userCode, @NotBlank String idToken) {}
}
