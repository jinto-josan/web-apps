package com.youtube.userprofileservice.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Query to retrieve notification settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetNotificationSettingsQuery {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
}

