package com.youtube.videotranscodeservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DRMConfig {
    private List<String> drmTypes; // ["Widevine", "FairPlay", "PlayReady"]
    private ContentKeyPolicy contentKeyPolicy;
    private String keyDeliveryUrl;
}

