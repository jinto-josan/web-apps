package com.youtube.userprofileservice.application.commands;

import com.youtube.userprofileservice.domain.valueobjects.CaptionFontSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Command to update accessibility preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccessibilityPreferencesCommand {
    
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;
    
    private Boolean captionsAlwaysOn;
    
    @Size(max = 10, message = "Captions language code cannot exceed 10 characters")
    private String captionsLanguage;
    
    private Boolean autoplayDefault;
    
    private Boolean autoplayOnHome;
    
    private CaptionFontSize captionsFontSize;
    
    private Integer captionsBackgroundOpacity;
    
    private String etag; // For optimistic locking
}

