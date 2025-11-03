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
import com.youtube.identityauthservice.domain.entities.Session;
import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.domain.events.UserCreated;
import com.youtube.identityauthservice.domain.repositories.UserRepository;
import com.youtube.identityauthservice.domain.services.EventPublisher;
import com.youtube.identityauthservice.domain.valueobjects.SessionId;
import com.youtube.identityauthservice.infrastructure.persistence.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthUseCaseImpl Tests")
class AuthUseCaseImplTest {

    @Mock
    private OidcIdTokenVerifier verifier;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRepositoryImpl userRepositoryImpl;

    @Mock
    private SessionRefreshService sessionService;

    @Mock
    private TokenService tokenService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private IdGenerator<UserId> userIdGenerator;

    @Mock
    private Clock clock;

    @Mock
    private UnitOfWork unitOfWork;

    private AuthUseCaseImpl authUseCase;
    private static final int ACCESS_TTL = 3600;
    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        authUseCase = new AuthUseCaseImpl(
                verifier,
                userRepository,
                userRepositoryImpl,
                sessionService,
                tokenService,
                eventPublisher,
                userIdGenerator,
                clock,
                unitOfWork,
                ACCESS_TTL
        );
        // Use lenient stubbing for commonly used mocks that may not be used in all tests
        lenient().when(clock.now()).thenReturn(NOW);
        lenient().doNothing().when(unitOfWork).begin();
        lenient().doNothing().when(unitOfWork).rollback(any(Exception.class));
    }

    @Nested
    @DisplayName("ExchangeToken Tests")
    class ExchangeTokenTests {

        @Test
        @DisplayName("Should exchange token for new user and create user")
        void shouldExchangeTokenForNewUser() {
            // Given
            String idToken = "valid-id-token";
            String email = "newuser@example.com";
            String name = "New User";
            String normalizedEmail = email.toLowerCase();
            
            ExchangeTokenCommand command = ExchangeTokenCommand.builder()
                    .idToken(idToken)
                    .deviceId("device-123")
                    .userAgent("Mozilla/5.0")
                    .ip("192.168.1.1")
                    .scope("openid profile")
                    .build();

            UserId userId = UserId.from("user-123");
            String sessionIdString = "01ARZ3NDEKTSV4RRFFQ69G5FAV"; // Valid ULID format
            
            OidcIdTokenVerifier.VerifiedIdentity verifiedIdentity = 
                    new OidcIdTokenVerifier.VerifiedIdentity(
                            "subject-123",
                            "https://login.microsoftonline.com/tenant/v2.0",
                            java.util.List.of("client-id"),
                            email,
                            name,
                            true,
                            null
                    );

            User newUser = User.builder()
                    .id(userId)
                    .email(email)
                    .normalizedEmail(normalizedEmail)
                    .displayName(name)
                    .emailVerified(true)
                    .status((short) 1)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            Session session = Session.builder()
                    .id(SessionId.from(sessionIdString))
                    .userId(userId)
                    .jti("jti-123")
                    .deviceId("device-123")
                    .userAgent("Mozilla/5.0")
                    .ip("192.168.1.1")
                    .build();

            SessionRefreshService.SessionWithRefresh sessionResult = 
                    new SessionRefreshService.SessionWithRefresh(
                            session,
                            "refresh-token-raw",
                            null
                    );

            when(verifier.verify(idToken)).thenReturn(verifiedIdentity);
            when(userRepository.findByNormalizedEmail(normalizedEmail)).thenReturn(Optional.empty());
            when(userIdGenerator.nextId()).thenReturn(userId);
            doNothing().when(userRepository).save(any(User.class));
            when(userRepositoryImpl.saveUser(any(User.class))).thenReturn(newUser);
            when(sessionService.createSessionWithRefresh(eq(userId), eq("device-123"), eq("Mozilla/5.0"), eq("192.168.1.1")))
                    .thenReturn(sessionResult);
            when(tokenService.newAccessToken(eq(userId.asString()), eq(sessionIdString), eq("openid profile"), anyMap()))
                    .thenReturn("access-token-123");

            // When
            AuthUseCase.TokenResponse response = authUseCase.exchangeToken(command);

            // Then
            assertNotNull(response);
            assertEquals("access-token-123", response.accessToken());
            assertEquals("refresh-token-raw", response.refreshToken());
            assertEquals("Bearer", response.tokenType());
            assertEquals(ACCESS_TTL, response.expiresIn());
            assertEquals("openid profile", response.scope());

            verify(verifier).verify(idToken);
            verify(userRepository).findByNormalizedEmail(normalizedEmail);
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishUserCreated(any(UserCreated.class));
            verify(sessionService).createSessionWithRefresh(eq(userId), eq("device-123"), eq("Mozilla/5.0"), eq("192.168.1.1"));
            verify(tokenService).newAccessToken(eq(userId.asString()), eq(sessionIdString), eq("openid profile"), anyMap());
        }

        @Test
        @DisplayName("Should exchange token for existing user and update user")
        void shouldExchangeTokenForExistingUser() {
            // Given
            String idToken = "valid-id-token";
            String email = "existing@example.com";
            String normalizedEmail = email.toLowerCase();
            String newName = "Updated Name";
            
            ExchangeTokenCommand command = ExchangeTokenCommand.builder()
                    .idToken(idToken)
                    .deviceId("device-123")
                    .userAgent("Mozilla/5.0")
                    .ip("192.168.1.1")
                    .build();

            UserId userId = UserId.from("user-123");
            String sessionIdString = "01ARZ3NDEKTSV4RRFFQ69G5FAV"; // Valid ULID format
            
            User existingUser = User.builder()
                    .id(userId)
                    .email("old@example.com")
                    .normalizedEmail(normalizedEmail)
                    .displayName("Old Name")
                    .emailVerified(false)
                    .status((short) 1)
                    .createdAt(NOW.minusSeconds(86400))
                    .updatedAt(NOW.minusSeconds(86400))
                    .version(1)
                    .build();

            User updatedUser = existingUser
                    .withEmail(email)
                    .withDisplayName(newName)
                    .withEmailVerified(true)
                    .markUpdated();

            OidcIdTokenVerifier.VerifiedIdentity verifiedIdentity = 
                    new OidcIdTokenVerifier.VerifiedIdentity(
                            "subject-123",
                            "https://login.microsoftonline.com/tenant/v2.0",
                            java.util.List.of("client-id"),
                            email,
                            newName,
                            true,
                            null
                    );

            Session session = Session.builder()
                    .id(SessionId.from(sessionIdString))
                    .userId(userId)
                    .jti("jti-123")
                    .deviceId("device-123")
                    .userAgent("Mozilla/5.0")
                    .ip("192.168.1.1")
                    .build();

            SessionRefreshService.SessionWithRefresh sessionResult = 
                    new SessionRefreshService.SessionWithRefresh(
                            session,
                            "refresh-token-raw",
                            null
                    );

            when(verifier.verify(idToken)).thenReturn(verifiedIdentity);
            when(userRepository.findByNormalizedEmail(normalizedEmail)).thenReturn(Optional.of(existingUser));
            doNothing().when(userRepository).save(any(User.class));
            when(userRepositoryImpl.saveUser(any(User.class))).thenReturn(updatedUser);
            when(sessionService.createSessionWithRefresh(eq(userId), eq("device-123"), eq("Mozilla/5.0"), eq("192.168.1.1")))
                    .thenReturn(sessionResult);
            when(tokenService.newAccessToken(eq(userId.asString()), eq(sessionIdString), anyString(), anyMap()))
                    .thenReturn("access-token-123");

            // When
            AuthUseCase.TokenResponse response = authUseCase.exchangeToken(command);

            // Then
            assertNotNull(response);
            assertEquals("access-token-123", response.accessToken());
            verify(userRepository).save(argThat(user -> 
                    user.getEmail().equals(email) && 
                    user.getDisplayName().equals(newName) &&
                    user.isEmailVerified()
            ));
            verify(eventPublisher, never()).publishUserCreated(any());
        }

        @Test
        @DisplayName("Should throw exception when email claim is missing")
        void shouldThrowExceptionWhenEmailClaimMissing() {
            // Given
            ExchangeTokenCommand command = ExchangeTokenCommand.builder()
                    .idToken("token-without-email")
                    .build();

            OidcIdTokenVerifier.VerifiedIdentity verifiedIdentity = 
                    new OidcIdTokenVerifier.VerifiedIdentity(
                            "subject-123",
                            "https://login.microsoftonline.com/tenant/v2.0",
                            java.util.List.of("client-id"),
                            null, // email is null
                            "Name",
                            false,
                            null
                    );

            when(verifier.verify("token-without-email")).thenReturn(verifiedIdentity);

            // When & Then
            assertThrows(SecurityException.class, () -> authUseCase.exchangeToken(command));
            verify(verifier).verify("token-without-email");
            verify(userRepository, never()).save(any());
            verify(userRepository, never()).findByNormalizedEmail(anyString());
        }

        @Test
        @DisplayName("Should use default scope when scope not provided")
        void shouldUseDefaultScopeWhenScopeNotProvided() {
            // Given
            String idToken = "valid-id-token";
            String email = "test@example.com";
            String normalizedEmail = email.toLowerCase();
            
            ExchangeTokenCommand command = ExchangeTokenCommand.builder()
                    .idToken(idToken)
                    .build(); // no scope

            UserId userId = UserId.from("user-123");
            String sessionIdString = "01ARZ3NDEKTSV4RRFFQ69G5FAV"; // Valid ULID format
            
            OidcIdTokenVerifier.VerifiedIdentity verifiedIdentity = 
                    new OidcIdTokenVerifier.VerifiedIdentity(
                            "subject-123",
                            "issuer",
                            java.util.List.of("audience"),
                            email,
                            "Name",
                            true,
                            null
                    );

            User newUser = User.builder()
                    .id(userId)
                    .email(email)
                    .normalizedEmail(normalizedEmail)
                    .displayName("Name")
                    .emailVerified(true)
                    .status((short) 1)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            Session session = Session.builder()
                    .id(SessionId.from(sessionIdString))
                    .userId(userId)
                    .jti("jti-123")
                    .deviceId("browser")
                    .userAgent("")
                    .ip("")
                    .build();

            SessionRefreshService.SessionWithRefresh sessionResult = 
                    new SessionRefreshService.SessionWithRefresh(session, "refresh", null);

            when(verifier.verify(idToken)).thenReturn(verifiedIdentity);
            when(userRepository.findByNormalizedEmail(normalizedEmail)).thenReturn(Optional.empty());
            when(userIdGenerator.nextId()).thenReturn(userId);
            doNothing().when(userRepository).save(any(User.class));
            when(userRepositoryImpl.saveUser(any(User.class))).thenReturn(newUser);
            when(sessionService.createSessionWithRefresh(any(), any(), any(), any())).thenReturn(sessionResult);
            when(tokenService.newAccessToken(anyString(), anyString(), eq("openid profile offline_access"), anyMap()))
                    .thenReturn("access-token");

            // When
            AuthUseCase.TokenResponse response = authUseCase.exchangeToken(command);

            // Then
            assertEquals("openid profile offline_access", response.scope());
            verify(tokenService).newAccessToken(anyString(), anyString(), eq("openid profile offline_access"), anyMap());
        }
    }

    @Nested
    @DisplayName("RefreshToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            // Given
            String refreshToken = "valid-refresh-token";
            RefreshTokenCommand command = RefreshTokenCommand.builder()
                    .refreshToken(refreshToken)
                    .scope("openid profile")
                    .build();

            UserId userId = UserId.from("user-123");
            String sessionIdString = "01ARZ3NDEKTSV4RRFFQ69G5FAV"; // Valid ULID format
            
            Session session = Session.builder()
                    .id(SessionId.from(sessionIdString))
                    .userId(userId)
                    .jti("jti-123")
                    .deviceId("device-123")
                    .userAgent("Mozilla/5.0")
                    .ip("192.168.1.1")
                    .build();

            SessionRefreshService.RotationResult rotationResult = 
                    new SessionRefreshService.RotationResult(
                            session,
                            "new-refresh-token",
                            null
                    );

            User user = User.builder()
                    .id(userId)
                    .email("test@example.com")
                    .normalizedEmail("test@example.com")
                    .displayName("Test User")
                    .emailVerified(true)
                    .status((short) 1)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            when(sessionService.rotateRefreshOrThrow(refreshToken)).thenReturn(rotationResult);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(tokenService.newAccessToken(eq(userId.asString()), eq(sessionIdString), eq("openid profile"), anyMap()))
                    .thenReturn("new-access-token");

            // When
            AuthUseCase.TokenResponse response = authUseCase.refreshToken(command);

            // Then
            assertNotNull(response);
            assertEquals("new-access-token", response.accessToken());
            assertEquals("new-refresh-token", response.refreshToken());
            assertEquals("Bearer", response.tokenType());
            assertEquals("openid profile", response.scope());

            verify(sessionService).rotateRefreshOrThrow(refreshToken);
            verify(userRepository).findById(userId);
            verify(tokenService).newAccessToken(eq(userId.asString()), eq(sessionIdString), eq("openid profile"), anyMap());
        }

        @Test
        @DisplayName("Should throw exception when user not found during refresh")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            RefreshTokenCommand command = RefreshTokenCommand.builder()
                    .refreshToken("refresh-token")
                    .build();

            UserId userId = UserId.from("user-123");
            String sessionIdString = "01ARZ3NDEKTSV4RRFFQ69G5FAV"; // Valid ULID format
            
            Session session = Session.builder()
                    .id(SessionId.from(sessionIdString))
                    .userId(userId)
                    .jti("jti-123")
                    .build();

            SessionRefreshService.RotationResult rotationResult = 
                    new SessionRefreshService.RotationResult(session, "new-refresh", null);

            when(sessionService.rotateRefreshOrThrow("refresh-token")).thenReturn(rotationResult);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(SecurityException.class, () -> authUseCase.refreshToken(command));
            
            verify(sessionService).rotateRefreshOrThrow("refresh-token");
            verify(userRepository).findById(userId);
            verify(unitOfWork).rollback(any(SecurityException.class));
        }

        @Test
        @DisplayName("Should rollback on exception during refresh")
        void shouldRollbackOnExceptionDuringRefresh() {
            // Given
            RefreshTokenCommand command = RefreshTokenCommand.builder()
                    .refreshToken("invalid-token")
                    .build();

            when(sessionService.rotateRefreshOrThrow("invalid-token"))
                    .thenThrow(new SecurityException("Invalid refresh token"));

            // When & Then
            assertThrows(SecurityException.class, () -> authUseCase.refreshToken(command));
            
            verify(unitOfWork).begin();
            verify(sessionService).rotateRefreshOrThrow("invalid-token");
            verify(unitOfWork).rollback(any(SecurityException.class));
            verify(userRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("RevokeSession Tests")
    class RevokeSessionTests {

        @Test
        @DisplayName("Should revoke session successfully")
        void shouldRevokeSessionSuccessfully() {
            // Given
            String refreshToken = "refresh-token-to-revoke";
            RevokeSessionCommand command = RevokeSessionCommand.builder()
                    .refreshToken(refreshToken)
                    .build();

            // sessionService.revokeByRawRefreshToken returns void, so we don't need to mock it
            // When
            doNothing().when(sessionService).revokeByRawRefreshToken(refreshToken);
            assertDoesNotThrow(() -> authUseCase.revokeSession(command));

            // Then
            verify(sessionService).revokeByRawRefreshToken(refreshToken);
        }

        @Test
        @DisplayName("Should rollback on exception during revoke")
        void shouldRollbackOnExceptionDuringRevoke() {
            // Given
            RevokeSessionCommand command = RevokeSessionCommand.builder()
                    .refreshToken("invalid-token")
                    .build();

            doThrow(new SecurityException("Token not found"))
                    .when(sessionService).revokeByRawRefreshToken("invalid-token");

            // When & Then
            assertThrows(SecurityException.class, () -> authUseCase.revokeSession(command));
            
            verify(unitOfWork).begin();
            verify(sessionService).revokeByRawRefreshToken("invalid-token");
            verify(unitOfWork).rollback(any(SecurityException.class));
        }
    }

    @Nested
    @DisplayName("GetUser Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            // Given
            UserId userId = UserId.from("user-123");
            GetUserQuery query = GetUserQuery.builder()
                    .userId(userId.asString())
                    .build();

            User user = User.builder()
                    .id(userId)
                    .email("test@example.com")
                    .normalizedEmail("test@example.com")
                    .displayName("Test User")
                    .emailVerified(true)
                    .status((short) 1)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .version(0)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // When
            User result = authUseCase.getUser(query);

            // Then
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("test@example.com", result.getEmail());
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            UserId userId = UserId.from("nonexistent-user");
            GetUserQuery query = GetUserQuery.builder()
                    .userId(userId.asString())
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authUseCase.getUser(query)
            );
            assertTrue(exception.getMessage().contains("User not found"));
        }
    }
}

