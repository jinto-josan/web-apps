package com.youtube.channelservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

/**
 * Embeddable class for channel branding information.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandingEmbeddable {
    
    @Column(name = "avatar_uri", length = 500)
    @Size(max = 500, message = "Avatar URI cannot exceed 500 characters")
    private String avatarUri;
    
    @Column(name = "banner_uri", length = 500)
    @Size(max = 500, message = "Banner URI cannot exceed 500 characters")
    private String bannerUri;
    
    @Column(name = "accent_color", length = 7)
    @Size(max = 7, message = "Accent color must be a valid hex color (max 7 characters)")
    private String accentColor;
}
