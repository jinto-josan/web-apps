package com.youtube.moderationservice.application.ports;

import com.youtube.moderationservice.domain.model.PolicyThreshold;

import java.util.List;

public interface PolicyRepository {
    List<PolicyThreshold> findAll();
}


