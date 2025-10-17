package com.youtube.identityauthservice.interfaces.rest;

import com.youtube.identityauthservice.application.usecases.ExchangeTokenUseCase;
import com.youtube.identityauthservice.application.usecases.LoginUseCase;
import com.youtube.identityauthservice.domain.entities.Jwks;
import com.youtube.identityauthservice.domain.services.KeyVaultSigner;
import com.youtube.identityauthservice.domain.valueobjects.TokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for AuthController.
 */
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private ExchangeTokenUseCase exchangeTokenUseCase;
    private LoginUseCase loginUseCase;
    private KeyVaultSigner keyVaultSigner;
    private AuthController controller;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exchangeTokenUseCase = mock(ExchangeTokenUseCase.class);
        loginUseCase = mock(LoginUseCase.class);
        keyVaultSigner = mock(KeyVaultSigner.class);
        
        controller = new AuthController(exchangeTokenUseCase, loginUseCase, keyVaultSigner);
        
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("192.168.1.1");
        mockRequest.addHeader("User-Agent", "Mozilla/5.0");
        mockRequest.addHeader("X-Device-Id", "device-123");
    }

    @Nested
    @DisplayName("Token Exchange Tests")
    class TokenExchangeTests {
        
        @Test
        @DisplayName("Should exchange token successfully")
        void shouldExchangeTokenSuccessfully() {
            // Given
            ExchangeRequest request = new ExchangeRequest("valid-id-token");
            TokenPair tokenPair = TokenPair.of(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(3600)
            );
            
            when(exchangeTokenUseCase.execute(anyString(), any()))
                .thenReturn(tokenPair);
            
            // When
            ResponseEntity<TokenResponse> response = controller.exchange(request, mockRequest);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("access-token", response.getBody().getAccessToken());
            assertEquals("refresh-token", response.getBody().getRefreshToken());
            assertTrue(response.getBody().getExpiresIn() > 0);
            
            verify(exchangeTokenUseCase).execute(eq("valid-id-token"), any());
        }
        
        @Test
        @DisplayName("Should return bad request for invalid request")
        void shouldReturnBadRequestForInvalidRequest() {
            // Given
            ExchangeRequest request = new ExchangeRequest(null);
            
            when(exchangeTokenUseCase.execute(anyString(), any()))
                .thenThrow(new IllegalArgumentException("Invalid request"));
            
            // When
            ResponseEntity<TokenResponse> response = controller.exchange(request, mockRequest);
            
            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return unauthorized for security exception")
        void shouldReturnUnauthorizedForSecurityException() {
            // Given
            ExchangeRequest request = new ExchangeRequest("invalid-token");
            
            when(exchangeTokenUseCase.execute(anyString(), any()))
                .thenThrow(new SecurityException("Invalid token"));
            
            // When
            ResponseEntity<TokenResponse> response = controller.exchange(request, mockRequest);
            
            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }
        
        @Test
        @DisplayName("Should return internal server error for unexpected exception")
        void shouldReturnInternalServerErrorForUnexpectedException() {
            // Given
            ExchangeRequest request = new ExchangeRequest("valid-token");
            
            when(exchangeTokenUseCase.execute(anyString(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));
            
            // When
            ResponseEntity<TokenResponse> response = controller.exchange(request, mockRequest);
            
            // Then
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        
        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {
            // Given
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            TokenPair tokenPair = TokenPair.of(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(3600)
            );
            
            when(loginUseCase.login(any(), any(), any()))
                .thenReturn(tokenPair);
            
            // When
            ResponseEntity<TokenResponse> response = controller.login(request, mockRequest);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("access-token", response.getBody().getAccessToken());
            assertEquals("refresh-token", response.getBody().getRefreshToken());
            
            verify(loginUseCase).login(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should return unauthorized for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() {
            // Given
            LoginRequest request = new LoginRequest("test@example.com", "wrong-password");
            
            when(loginUseCase.login(any(), any(), any()))
                .thenThrow(new SecurityException("Invalid credentials"));
            
            // When
            ResponseEntity<TokenResponse> response = controller.login(request, mockRequest);
            
            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("JWKS Tests")
    class JwksTests {
        
        @Test
        @DisplayName("Should return JWKS successfully")
        void shouldReturnJwksSuccessfully() {
            // Given
            Jwks jwks = Jwks.of(List.of(Map.of(
                "kty", "RSA",
                "kid", "key-1",
                "use", "sig"
            )));
            
            when(keyVaultSigner.getJwks()).thenReturn(jwks);
            
            // When
            ResponseEntity<Jwks> response = controller.getJwks();
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(jwks, response.getBody());
            
            verify(keyVaultSigner).getJwks();
        }
        
        @Test
        @DisplayName("Should return internal server error for JWKS failure")
        void shouldReturnInternalServerErrorForJwksFailure() {
            // Given
            when(keyVaultSigner.getJwks())
                .thenThrow(new RuntimeException("JWKS error"));
            
            // When
            ResponseEntity<Jwks> response = controller.getJwks();
            
            // Then
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {
        
        @Test
        @DisplayName("Should return health status")
        void shouldReturnHealthStatus() {
            // When
            ResponseEntity<String> response = controller.health();
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("OK", response.getBody());
        }
    }

    @Nested
    @DisplayName("Client Info Extraction Tests")
    class ClientInfoExtractionTests {
        
        @Test
        @DisplayName("Should extract client info from request")
        void shouldExtractClientInfoFromRequest() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("User-Agent", "Test-Agent");
            request.addHeader("X-Device-Id", "test-device");
            
            ExchangeRequest exchangeRequest = new ExchangeRequest("token");
            TokenPair tokenPair = TokenPair.of(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(3600)
            );
            
            when(exchangeTokenUseCase.execute(anyString(), any()))
                .thenReturn(tokenPair);
            
            // When
            ResponseEntity<TokenResponse> response = controller.exchange(exchangeRequest, request);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(exchangeTokenUseCase).execute(eq("token"), any());
        }
        
        @Test
        @DisplayName("Should handle missing headers gracefully")
        void shouldHandleMissingHeadersGracefully() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("10.0.0.1");
            // No User-Agent or X-Device-Id headers
            
            ExchangeRequest exchangeRequest = new ExchangeRequest("token");
            TokenPair tokenPair = TokenPair.of(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(3600)
            );
            
            when(exchangeTokenUseCase.execute(anyString(), any()))
                .thenReturn(tokenPair);
            
            // When
            ResponseEntity<TokenResponse> response = controller.exchange(exchangeRequest, request);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(exchangeTokenUseCase).execute(eq("token"), any());
        }
    }
}
