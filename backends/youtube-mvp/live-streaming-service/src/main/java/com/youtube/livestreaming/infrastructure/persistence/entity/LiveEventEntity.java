package com.youtube.livestreaming.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "live_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveEventEntity {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "channel_id", nullable = false)
    private String channelId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "ams_live_event_id")
    private String amsLiveEventId;
    
    @Column(name = "ams_live_event_name")
    private String amsLiveEventName;
    
    @Column(name = "ingest_url", length = 1000)
    private String ingestUrl;
    
    @Column(name = "preview_url", length = 1000)
    private String previewUrl;
    
    @Column(name = "region")
    private String region;
    
    @Column(name = "dvr_enabled")
    private Boolean dvrEnabled;
    
    @Column(name = "dvr_window_in_minutes")
    private Integer dvrWindowInMinutes;
    
    @Column(name = "low_latency_enabled")
    private Boolean lowLatencyEnabled;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "started_at")
    private Instant startedAt;
    
    @Column(name = "stopped_at")
    private Instant stoppedAt;
    
    @Column(name = "archived_at")
    private Instant archivedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Version
    private Long version;
}

