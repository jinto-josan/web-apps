package com.youtube.mvp.videocatalog.domain.model;

import lombok.*;

/**
 * Duration value object (in seconds).
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Duration {
    private long seconds;
    
    public static Duration fromSeconds(long seconds) {
        return Duration.builder().seconds(seconds).build();
    }
    
    public String toISO8601() {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}

