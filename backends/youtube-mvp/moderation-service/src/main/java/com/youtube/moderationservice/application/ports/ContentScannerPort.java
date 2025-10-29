package com.youtube.moderationservice.application.ports;

import java.util.Map;

public interface ContentScannerPort {
    Map<String, Double> scanText(String content, Map<String, Object> context);
}


