package com.youtube.userprofileservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Value object representing accessibility and playback preferences.
 * Controls captions, autoplay, and playback behavior.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessibilityPreferences {
    
    @Builder.Default
    @NotNull(message = "Captions always on flag cannot be null")
    private Boolean captionsAlwaysOn = false;
    
    @Size(max = 10, message = "Captions language code cannot exceed 10 characters")
    private String captionsLanguage; // e.g., "en", "es", "fr"
    
    @Builder.Default
    @NotNull(message = "Autoplay default flag cannot be null")
    private Boolean autoplayDefault = false;
    
    @Builder.Default
    @NotNull(message = "Autoplay on home flag cannot be null")
    private Boolean autoplayOnHome = false;
    
    @Builder.Default
    @NotNull(message = "Captions font size cannot be null")
    private CaptionFontSize captionsFontSize = CaptionFontSize.MEDIUM;
    
    @Builder.Default
    @NotNull(message = "Captions background opacity cannot be null")
    private Integer captionsBackgroundOpacity = 100; // 0-100
}

