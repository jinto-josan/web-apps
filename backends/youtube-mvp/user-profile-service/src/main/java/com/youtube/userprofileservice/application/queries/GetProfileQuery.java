package com.youtube.userprofileservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Query to retrieve a user's profile.
 * Used in CQRS pattern for read operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProfileQuery {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
}

