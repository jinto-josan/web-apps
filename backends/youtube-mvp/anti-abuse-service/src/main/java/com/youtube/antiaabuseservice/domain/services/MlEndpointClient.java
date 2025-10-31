package com.youtube.antiaabuseservice.domain.services;

import java.util.Map;

public interface MlEndpointClient {
    /**
     * Call Azure ML online endpoint for risk prediction.
     */
    Map<String, Object> predict(Map<String, Object> features);
}

