package com.youtube.moderationservice.infrastructure.adapters.appconfig;

import com.youtube.moderationservice.application.ports.PolicyRepository;
import com.youtube.moderationservice.domain.model.PolicyThreshold;
import com.youtube.moderationservice.infrastructure.config.PolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicyRepositoryAdapter implements PolicyRepository {
    private final PolicyProperties properties;

    @Override
    public List<PolicyThreshold> findAll() {
        return properties.getPolicies();
    }
}


