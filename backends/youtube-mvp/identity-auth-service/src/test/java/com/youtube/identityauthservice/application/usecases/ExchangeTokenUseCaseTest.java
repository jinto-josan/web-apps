package com.youtube.identityauthservice.application.usecases;

import com.youtube.identityauthservice.domain.entities.DecodedToken;
import com.youtube.identityauthservice.domain.entities.Session;
import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.infrastructure.persistence.RefreshTokenRepository;
import com.youtube.identityauthservice.infrastructure.persistence.SessionRepository;
import com.youtube.identityauthservice.infrastructure.persistence.UserRepository;
import com.youtube.identityauthservice.domain.services.OidcVerifier;
import com.youtube.identityauthservice.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for ExchangeTokenUseCase.
 */
@DisplayName("ExchangeTokenUseCase Tests")
class ExchangeTokenUseCaseTest {

    @Mock
    private OidcVerifier oidcVerifier;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SessionRepository sessionRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private TokenService tokenService;
    
    private ExchangeTokenUseCase useCase;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new ExchangeTokenUseCase(
            oidcVerifier,
            userRepository,
            sessionRepository,
            refreshTokenRepository,
            tokenService
        );
    }

    @Nested
    @DisplayName("Successful Token Exchange Tests")
    class SuccessfulTokenExchangeTests {
        
        @Test
        @DisplayName("Should exchange token for existing user by AAD subject")
        void shouldExchangeTokenForExistingUserByAadSubject() {
            // Given
            String aadIdToken = "valid-id-token";
            ClientInfo clientInfo = ClientInfo.of("ip-hash", "user-agent", "device-id");
            
            DecodedToken decodedToken = new DecodedToken(
                "aad-subject-123",
                "test@example.com",
                "https://login.microsoftonline.com/tenant/v2.0",
                "client-id",
                Instant.now().plusSeconds(3600),
                Map.of()
            );
            
            User existingUser = User.create(
                UserId.generate(),
                Email.of("test@example.com"),
                "aad-subject-123"
            );
            
            Session session = Session.create(
                SessionId.generate(),
                existingUser.getId(),
                "user-agent",
                "ip-hash",
                "device-id",
                Instant.now().plusSeconds(3600)
            );
            
            TokenPair tokenPair = TokenPair.of(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(3600)
            );
            
            when(oidcVerifier.verifyIdToken(aadIdToken)).thenReturn(decodedToken);
            when(userRepository.findByAadSubject("aad-subject-123")).thenReturn(Optional.of(existingUser));
            when(sessionRepository.save(any(Session.class))).thenReturn(session);
            when(tokenService.issueFor(existingUser, session)).thenReturn(tokenPair);
            
            // When
            TokenPair result = useCase.execute(aadIdToken, clientInfo);
            
            // Then
            assertNotNull(result);
            assertEquals(tokenPair, result);
            
            verify(oidcVerifier).verifyIdToken(aadIdToken);
            verify(userRepository).findByAadSubject("aad-subject-123");
            verify(sessionRepository).save(any(Session.class));
            verify(tokenService).issueFor(existingUser, session);
        }
        
        @Test
        @DisplayName("Should exchange token for existing user by email")
        void shouldExchangeTokenForExistingUserByEmail() {
            // Given
            String aadIdToken = "valid-id-token";
            ClientInfo clientInfo = ClientInfo.of("ip-hash", "user-agent", "device-id");
            
            DecodedToken decodedToken = new DecodedToken(
                "aad-subject-123",
                "test@example.com",
                "https://login.microsoftonline.com/tenant/v2.0",
                "client-id",
                Instant.now().plusSeconds(3600),
                Map.of()
            );
            
            User existingUser = User.create(
                UserId.generate(),
                Email.of("test@example.com"),
                "old-aad-subject"
            );
            
            Session session = Session.create(
                SessionId.generate(),
                existingUser.getId(),
                "user-agent",
                "ip-hash",
                "device-id",
                Instant.now().plusSeconds(3600)
            );
            
            TokenPair tokenPair = TokenPair.of(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(3600)
            );
            
            when(oidcVerifier.verifyIdToken(aadIdToken)).thenReturn(decodedToken);
            when(userRepository.findByAadSubject("aad-subject-123")).thenReturn(Optional.empty());
            when(userRepository.findByEmail(Email.of("test@example.com"))).thenReturn(Optional.of(existingUser));
            when(userRepository.save(existingUser)).thenReturn(existingUser);
            when(sessionRepository.save(any(Session.class))).thenReturn(session);
            when(tokenService.issueFor(existingUser, session)).thenReturn(tokenPair);
            
            // When
            TokenPair result = useCase.execute(aadIdToken, clientInfo);
            
            // Then
            assertNotNull(result);
            assertEquals(tokenPair, result);
            
            verify(userRepository).findByAadSubject("aad-subject-123");
            verify(userRepository).findByEmail(Email.of("test@example.com"));
            verify(userRepository).save(existingUser);
        }
        
        @Test
        @DisplayName("Should exchange token for new user")
        void shouldExchangeTokenForNewUser() {
            // Given
            String aadIdToken = "valid-id-token";
            ClientInfo clientInfo = ClientInfo.of("ip-hash", "user-agent", "device-id");
            
            DecodedToken decodedToken = new DecodedToken(
                "aad-subject-123",
                "newuser@example.com",
                "https://login.microsoftonline.com/tenant/v2.0",
                "client-id",
                Instant.now().plusSeconds(3600),
                Map.of()
            );
            
            User newUser = User.create(
                UserId.generate(),
                Email.of("newuser@example.com"),
                "aad-subject-123"
            );
            
            Session session = Session.create(
                SessionId.generate(),
                newUser.getId(),
                "user-agent",
                "ip-hash",
                "device-id",
                Instant.now().plusSeconds(3600)
            );
            
            TokenPair tokenPair = TokenPair.of(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(3600)
            );
            
            when(oidcVerifier.verifyIdToken(aadIdToken)).thenReturn(decodedToken);
            when(userRepository.findByAadSubject("aad-subject-123")).thenReturn(Optional.empty());
            when(userRepository.findByEmail(Email.of("newuser@example.com"))).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(sessionRepository.save(any(Session.class))).thenReturn(session);
            when(tokenService.issueFor(newUser, session)).thenReturn(tokenPair);
            
            // When
            TokenPair result = useCase.execute(aadIdToken, clientInfo);
            
            // Then
            assertNotNull(result);
            assertEquals(tokenPair, result);
            
            verify(userRepository).findByAadSubject("aad-subject-123");
            verify(userRepository).findByEmail(Email.of("newuser@example.com"));
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should throw exception for invalid ID token")
        void shouldThrowExceptionForInvalidIdToken() {
            // Given
            String invalidIdToken = "invalid-token";
            ClientInfo clientInfo = ClientInfo.of("ip-hash", "user-agent", "device-id");
            
            when(oidcVerifier.verifyIdToken(invalidIdToken))
                .thenThrow(new SecurityException("Invalid token"));
            
            // When & Then
            assertThrows(SecurityException.class, () -> useCase.execute(invalidIdToken, clientInfo));
            
            verify(oidcVerifier).verifyIdToken(invalidIdToken);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(sessionRepository);
            verifyNoInteractions(tokenService);
        }
        
        @Test
        @DisplayName("Should throw exception for null parameters")
        void shouldThrowExceptionForNullParameters() {
            // When & Then
            assertThrows(NullPointerException.class, () -> useCase.execute(null, ClientInfo.of("ip", "agent", "device")));
            assertThrows(NullPointerException.class, () -> useCase.execute("token", null));
        }
    }
}
