package com.youtube.notificationsservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.cosmos.core.mapping.Container;
import org.springframework.data.cosmos.core.mapping.PartitionKey;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Container(containerName = "notification-preferences")
public class NotificationPreference {

    @Id
    private String id; // userId

    @PartitionKey
    private String tenantId;

    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;

    // Channel specific preferences, e.g., {"marketing": false, "alerts": true}
    private Map<String, Boolean> channelPreferences;

    @Version
    private String etag;
}


