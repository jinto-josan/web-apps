package com.youtube.channelservice.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * Represents channel branding information.
 * Immutable value object containing avatar, banner, and accent color.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branding {
    
    @Size(max = 500, message = "Avatar URI cannot exceed 500 characters")
    private String avatarUri;
    
    @Size(max = 500, message = "Banner URI cannot exceed 500 characters")
    private String bannerUri;
    
    @Size(max = 7, message = "Accent color must be a valid hex color (max 7 characters)")
    private String accentColor;
}