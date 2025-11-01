package com.youtube.identityauthservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to revoke a session (logout).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeSessionCommand {
    
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}

