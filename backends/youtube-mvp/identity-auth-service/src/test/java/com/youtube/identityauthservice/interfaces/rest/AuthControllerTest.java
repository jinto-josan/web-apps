package com.youtube.identityauthservice.interfaces.rest;

import com.youtube.identityauthservice.application.usecases.AuthUseCase;
import com.youtube.identityauthservice.interfaces.rest.dto.AuthDtos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthUseCase authUseCase;

    @InjectMocks
    private AuthController controller;

    @Nested
    @DisplayName("Exchange Token Tests")
    class ExchangeTokenTests {

        @Test
        @DisplayName("Should exchange token successfully")
        void shouldExchangeTokenSuccessfully() {
            // Given
            AuthDtos.ExchangeRequest request = new AuthDtos.ExchangeRequest(
                    "valid-id-token",
                    "device-123",
                    "Mozilla/5.0",
                    "192.168.1.1",
                    "openid profile"
            );

            AuthUseCase.TokenResponse tokenResponse = new AuthUseCase.TokenResponse(
                    "access-token-123",
                    "refresh-token-456",
                    "Bearer",
                    3600L,
                    "openid profile"
            );

            when(authUseCase.exchangeToken(any())).thenReturn(tokenResponse);

            // When
            AuthDtos.TokenResponse response = controller.exchange(request);

            // Then
            assertNotNull(response);
            assertEquals("access-token-123", response.accessToken());
            assertEquals("refresh-token-456", response.refreshToken());
            assertEquals("Bearer", response.tokenType());
            assertEquals(3600L, response.expiresIn());
            assertEquals("openid profile", response.scope());

            verify(authUseCase).exchangeToken(argThat(cmd ->
                    cmd.getIdToken().equals("valid-id-token") &&
                    cmd.getDeviceId().equals("device-123") &&
                    cmd.getUserAgent().equals("Mozilla/5.0") &&
                    cmd.getIp().equals("192.168.1.1") &&
                    cmd.getScope().equals("openid profile")
            ));
        }

        @Test
        @DisplayName("Should use default values for optional fields")
        void shouldUseDefaultValuesForOptionalFields() {
            // Given
            AuthDtos.ExchangeRequest request = new AuthDtos.ExchangeRequest(
                    "valid-id-token",
                    null,
                    null,
                    null,
                    null
            );

            AuthUseCase.TokenResponse tokenResponse = new AuthUseCase.TokenResponse(
                    "access-token",
                    "refresh-token",
                    "Bearer",
                    3600L,
                    "openid profile offline_access"
            );

            when(authUseCase.exchangeToken(any())).thenReturn(tokenResponse);

            // When
            AuthDtos.TokenResponse response = controller.exchange(request);

            // Then
            assertNotNull(response);
            verify(authUseCase).exchangeToken(argThat(cmd ->
                    cmd.getIdToken().equals("valid-id-token") &&
                    cmd.getDeviceId() == null &&
                    cmd.getUserAgent() == null &&
                    cmd.getIp() == null &&
                    cmd.getScope() == null
            ));
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            // Given
            AuthDtos.RefreshRequest request = new AuthDtos.RefreshRequest(
                    "valid-refresh-token",
                    "openid profile"
            );

            AuthUseCase.TokenResponse tokenResponse = new AuthUseCase.TokenResponse(
                    "new-access-token",
                    "new-refresh-token",
                    "Bearer",
                    3600L,
                    "openid profile"
            );

            when(authUseCase.refreshToken(any())).thenReturn(tokenResponse);

            // When
            AuthDtos.TokenResponse response = controller.refresh(request);

            // Then
            assertNotNull(response);
            assertEquals("new-access-token", response.accessToken());
            assertEquals("new-refresh-token", response.refreshToken());

            verify(authUseCase).refreshToken(argThat(cmd ->
                    cmd.getRefreshToken().equals("valid-refresh-token") &&
                    cmd.getScope().equals("openid profile")
            ));
        }

        @Test
        @DisplayName("Should use default scope when not provided")
        void shouldUseDefaultScopeWhenNotProvided() {
            // Given
            AuthDtos.RefreshRequest request = new AuthDtos.RefreshRequest(
                    "valid-refresh-token",
                    null
            );

            AuthUseCase.TokenResponse tokenResponse = new AuthUseCase.TokenResponse(
                    "access-token",
                    "refresh-token",
                    "Bearer",
                    3600L,
                    "openid profile offline_access"
            );

            when(authUseCase.refreshToken(any())).thenReturn(tokenResponse);

            // When
            AuthDtos.TokenResponse response = controller.refresh(request);

            // Then
            assertNotNull(response);
            verify(authUseCase).refreshToken(argThat(cmd ->
                    cmd.getRefreshToken().equals("valid-refresh-token") &&
                    cmd.getScope() == null
            ));
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() {
            // Given
            AuthDtos.LogoutRequest request = new AuthDtos.LogoutRequest("refresh-token-to-revoke");

            doNothing().when(authUseCase).revokeSession(any());

            // When
            Map<String, Object> response = controller.logout(request);

            // Then
            assertNotNull(response);
            assertTrue((Boolean) response.get("ok"));

            verify(authUseCase).revokeSession(argThat(cmd ->
                    cmd.getRefreshToken().equals("refresh-token-to-revoke")
            ));
        }
    }
}
