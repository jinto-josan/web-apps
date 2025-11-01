package com.youtube.identityauthservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to exchange an OIDC ID token for platform tokens.
 * Used in CQRS pattern for write operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeTokenCommand {
    
    @NotBlank(message = "ID token cannot be blank")
    private String idToken;
    
    private String deviceId;
    
    private String userAgent;
    
    private String ip;
    
    private String scope;
}

