package com.youtube.identityauthservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to verify user email address.
 * Used in CQRS pattern for write operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailCommand {
    
    @NotBlank(message = "Verification token cannot be blank")
    private String token;
    
    private String deviceId;
    
    private String userAgent;
    
    private String ip;
}

