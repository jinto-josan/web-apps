package com.youtube.adsdecisionservice.application.services;

import com.youtube.adsdecisionservice.interfaces.rest.dto.AdDecisionRequest;
import com.youtube.adsdecisionservice.interfaces.rest.dto.AdDecisionResponse;
import org.springframework.stereotype.Service;

@Service
public class DecisionServiceImpl implements DecisionService {

    @Override
    public AdDecisionResponse decide(AdDecisionRequest request) {
        // Placeholder decision logic; will be replaced by domain decision engine wiring
        return AdDecisionResponse.builder()
                .campaignId(null)
                .creativeId(null)
                .fill(false)
                .reason("no-implementation")
                .build();
    }
}


