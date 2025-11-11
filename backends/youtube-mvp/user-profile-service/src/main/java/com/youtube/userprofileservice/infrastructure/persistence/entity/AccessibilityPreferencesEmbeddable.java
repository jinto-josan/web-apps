package com.youtube.userprofileservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable JPA entity for accessibility preferences.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessibilityPreferencesEmbeddable {
    
    @Column(name = "accessibility_captions_always_on")
    private Boolean captionsAlwaysOn;
    
    @Column(name = "accessibility_captions_language", length = 10)
    private String captionsLanguage;
    
    @Column(name = "accessibility_autoplay_default")
    private Boolean autoplayDefault;
    
    @Column(name = "accessibility_autoplay_on_home")
    private Boolean autoplayOnHome;
    
    @Column(name = "accessibility_captions_font_size")
    private String captionsFontSize;
    
    @Column(name = "accessibility_captions_background_opacity")
    private Integer captionsBackgroundOpacity;
}

