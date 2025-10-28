package com.youtube.videotranscodeservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DRMConfiguration {
    private Map<String, String> contentKeys; // {Widevine, FairPlay, PlayReady} -> key ID
    private Map<String, String> licenseUrls;
    private ContentKeyPolicy contentKeyPolicy;
}

