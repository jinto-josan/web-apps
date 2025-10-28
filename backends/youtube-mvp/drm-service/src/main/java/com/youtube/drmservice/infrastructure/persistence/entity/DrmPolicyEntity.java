package com.youtube.drmservice.infrastructure.persistence.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import java.time.Instant;

@Entity
@Table(name = "drm_policies", indexes = {
    @Index(name = "idx_video_id", columnList = "video_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrmPolicyEntity {

    @Id
    private String id;

    @Column(name = "video_id", nullable = false, unique = true)
    private String videoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private DrmProvider provider;

    @Type(JsonType.class)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private PolicyConfigurationEmbeddable configuration;

    @Type(JsonType.class)
    @Column(name = "rotation_policy", columnDefinition = "jsonb")
    private KeyRotationPolicyEmbeddable rotationPolicy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;

    public enum DrmProvider {
        WIDEVINE, PLAYREADY, FAIRPLAY
    }
}

