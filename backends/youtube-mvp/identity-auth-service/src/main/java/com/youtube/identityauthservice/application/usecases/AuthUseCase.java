package com.youtube.identityauthservice.application.usecases;

import com.youtube.identityauthservice.application.commands.ExchangeTokenCommand;
import com.youtube.identityauthservice.application.commands.RefreshTokenCommand;
import com.youtube.identityauthservice.application.commands.RevokeSessionCommand;
import com.youtube.identityauthservice.application.queries.GetUserQuery;
import com.youtube.identityauthservice.domain.entities.User;

/**
 * Use case interface for authentication operations.
 * Follows CQRS pattern with separate read and write operations.
 */
public interface AuthUseCase {
    
    /**
     * Exchanges an OIDC ID token for platform tokens.
     * Creates or updates user if needed.
     * 
     * @param command the exchange command
     * @return token response with access and refresh tokens
     */
    TokenResponse exchangeToken(ExchangeTokenCommand command);
    
    /**
     * Refreshes an access token using a refresh token.
     * 
     * @param command the refresh command
     * @return token response with new access and refresh tokens
     */
    TokenResponse refreshToken(RefreshTokenCommand command);
    
    /**
     * Revokes a session (logout).
     * 
     * @param command the revoke command
     */
    void revokeSession(RevokeSessionCommand command);
    
    /**
     * Gets a user by ID.
     * 
     * @param query the get user query
     * @return the user
     */
    User getUser(GetUserQuery query);
    
    /**
     * Token response containing access and refresh tokens.
     */
    record TokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            String scope
    ) {}
}

