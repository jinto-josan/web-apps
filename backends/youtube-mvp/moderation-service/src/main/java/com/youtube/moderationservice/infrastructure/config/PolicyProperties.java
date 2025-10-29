package com.youtube.moderationservice.infrastructure.config;

import com.youtube.moderationservice.domain.model.PolicyThreshold;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "moderation")
@Data
public class PolicyProperties {
    private List<PolicyThreshold> policies = new ArrayList<>();
}


