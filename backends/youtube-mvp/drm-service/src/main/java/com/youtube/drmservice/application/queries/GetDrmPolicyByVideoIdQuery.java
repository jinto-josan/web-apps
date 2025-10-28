package com.youtube.drmservice.application.queries;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetDrmPolicyByVideoIdQuery {
    String videoId;
}

