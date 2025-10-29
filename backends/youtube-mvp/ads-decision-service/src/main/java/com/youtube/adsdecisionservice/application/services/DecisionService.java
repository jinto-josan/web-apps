package com.youtube.adsdecisionservice.application.services;

import com.youtube.adsdecisionservice.interfaces.rest.dto.AdDecisionRequest;
import com.youtube.adsdecisionservice.interfaces.rest.dto.AdDecisionResponse;

public interface DecisionService {
    AdDecisionResponse decide(AdDecisionRequest request);
}


