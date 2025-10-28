package com.youtube.videotranscodeservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResult {
    private String jobId;
    private JobStatus status;
    private String assetId;
    private String errorMessage;
}

