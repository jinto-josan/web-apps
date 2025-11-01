package com.youtube.identityauthservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Command to setup Multi-Factor Authentication.
 * Used in CQRS pattern for write operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupMfaCommand {
    
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
}

