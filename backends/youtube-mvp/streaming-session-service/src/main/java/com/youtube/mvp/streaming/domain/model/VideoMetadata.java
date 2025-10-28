package com.youtube.mvp.streaming.domain.model;

import lombok.*;
import java.util.List;

/**
 * Video metadata for playback.
 */
@Getter
@Builder
@ToString
public class VideoMetadata {
    private String videoId;
    private String title;
    private String videoFormat; // hls, dash, mp4
    private String drmType; // widevine, playready, fairplay
    private List<String> allowedRegions;
    private List<String> blockedRegions;
    private String visibility; // PUBLIC, PRIVATE, UNLISTED
    private long duration;
    private String manifestUrl;
}

