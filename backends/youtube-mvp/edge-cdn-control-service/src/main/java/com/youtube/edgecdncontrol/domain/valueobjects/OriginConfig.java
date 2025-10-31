package com.youtube.edgecdncontrol.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OriginConfig {
    String originName;
    String originHost;
    int priority;
    int weight;
    boolean enabled;
    List<String> healthProbePaths;
    int timeoutSeconds;
    int healthProbeIntervalSeconds;
}

