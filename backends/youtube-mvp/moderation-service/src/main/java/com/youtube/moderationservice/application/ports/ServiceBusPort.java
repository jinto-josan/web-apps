package com.youtube.moderationservice.application.ports;

public interface ServiceBusPort {
    void publishReviewTask(String caseId, String payload);
}


