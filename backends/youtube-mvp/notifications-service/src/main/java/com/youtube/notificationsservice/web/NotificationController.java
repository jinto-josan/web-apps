package com.youtube.notificationsservice.web;

import com.youtube.notificationsservice.application.service.TestNotificationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
public class NotificationController {

    private final TestNotificationService testNotificationService;

    public NotificationController(TestNotificationService testNotificationService) {
        this.testNotificationService = testNotificationService;
    }

    @PostMapping("/test")
    public ResponseEntity<Void> test(
            @RequestParam @NotBlank String tenantId,
            @RequestParam @NotBlank String userId,
            @RequestParam(defaultValue = "email") String channel) {
        testNotificationService.enqueueTest(tenantId, userId, channel);
        return ResponseEntity.accepted().build();
    }
}


