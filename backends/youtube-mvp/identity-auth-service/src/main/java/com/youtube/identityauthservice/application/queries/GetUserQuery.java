package com.youtube.identityauthservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Query to get a user by ID.
 * Used in CQRS pattern for read operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserQuery {
    
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
}

