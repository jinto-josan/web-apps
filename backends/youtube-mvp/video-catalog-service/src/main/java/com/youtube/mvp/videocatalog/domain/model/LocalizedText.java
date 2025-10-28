package com.youtube.mvp.videocatalog.domain.model;

import lombok.*;

/**
 * Localized text value object.
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class LocalizedText {
    private String language;
    private String text;
}

