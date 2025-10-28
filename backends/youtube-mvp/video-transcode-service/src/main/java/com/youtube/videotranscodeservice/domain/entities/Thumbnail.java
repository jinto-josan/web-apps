package com.youtube.videotranscodeservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Thumbnail {
    private UUID thumbnailId;
    private UUID videoId;
    private String url;
    private String timeCode;
    private ThumbnailSize size;
    private boolean selected;
    private Instant createdAt;
    
    public void markAsSelected() {
        this.selected = true;
    }
    
    public void markAsUnselected() {
        this.selected = false;
    }
}

