package com.youtube.mvp.streaming.domain.model;

import lombok.*;

/**
 * Device information value object.
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class DeviceInfo {
    private String deviceId;
    private String userAgent;
    private String ipAddress;
    private String countryCode;
    private String region;
    private DeviceType deviceType;
    private String os;
    private String browser;
}

