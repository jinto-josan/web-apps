package com.youtube.videotranscodeservice.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaProcessingJob {
    private String jobId;
    private UUID videoId;
    private UUID userId;
    private ProcessingStatus status;
    private String azureJobId;
    private String assetId;
    private String transformId;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private String errorMessage;
    private Map<String, String> metadata;
    
    public void updateStatus(ProcessingStatus newStatus) {
        this.status = newStatus;
        if (newStatus == ProcessingStatus.ENCODING && this.startedAt == null) {
            this.startedAt = Instant.now();
        }
        if (newStatus == ProcessingStatus.COMPLETED || newStatus == ProcessingStatus.FAILED) {
            this.completedAt = Instant.now();
        }
    }
    
    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    public void setError(String error) {
        this.errorMessage = error;
        this.status = ProcessingStatus.FAILED;
        this.completedAt = Instant.now();
    }
    
    public boolean isCompleted() {
        return status == ProcessingStatus.COMPLETED || status == ProcessingStatus.FAILED;
    }
    
    public long getProcessingDurationMs() {
        if (startedAt == null) return 0;
        Instant endTime = completedAt != null ? completedAt : Instant.now();
        return endTime.toEpochMilli() - startedAt.toEpochMilli();
    }
}

