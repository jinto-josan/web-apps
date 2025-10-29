package com.youtube.notificationsservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationPreferenceDto {
    @NotBlank
    private String userId;
    @NotBlank
    private String tenantId;
    @NotNull
    private Boolean emailEnabled;
    @NotNull
    private Boolean pushEnabled;
    @NotNull
    private Boolean inAppEnabled;
    private Map<String, Boolean> channelPreferences;
}


