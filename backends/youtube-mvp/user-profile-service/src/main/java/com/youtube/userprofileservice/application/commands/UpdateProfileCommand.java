package com.youtube.userprofileservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Command to update a user's profile.
 * Used in CQRS pattern for write operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileCommand {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
    
    private String displayName;
    
    @Size(max = 500, message = "Photo URL cannot exceed 500 characters")
    private String photoUrl;
    
    @Size(max = 10, message = "Locale code cannot exceed 10 characters")
    private String locale;
    
    @Size(max = 5, message = "Country code cannot exceed 5 characters")
    private String country;
    
    @Size(max = 100, message = "Timezone cannot exceed 100 characters")
    private String timezone;
    
    private String etag; // For optimistic locking
}

