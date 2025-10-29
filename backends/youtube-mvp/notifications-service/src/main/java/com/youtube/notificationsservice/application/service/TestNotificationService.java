package com.youtube.notificationsservice.application.service;

import com.youtube.notificationsservice.application.port.JobPublisherPort;
import org.springframework.stereotype.Service;

@Service
public class TestNotificationService {

    private final JobPublisherPort jobPublisherPort;

    public TestNotificationService(JobPublisherPort jobPublisherPort) {
        this.jobPublisherPort = jobPublisherPort;
    }

    public void enqueueTest(String tenantId, String userId, String channel) {
        jobPublisherPort.publishTestNotificationJob(tenantId, userId, channel);
    }
}


