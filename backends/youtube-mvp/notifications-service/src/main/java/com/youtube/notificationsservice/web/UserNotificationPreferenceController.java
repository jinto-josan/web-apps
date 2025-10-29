package com.youtube.notificationsservice.web;

import com.youtube.notificationsservice.application.dto.NotificationPreferenceDto;
import com.youtube.notificationsservice.application.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users/{userId}/notification-prefs")
@Validated
public class UserNotificationPreferenceController {

    private final NotificationPreferenceService service;

    public UserNotificationPreferenceController(NotificationPreferenceService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<NotificationPreferenceDto> get(@PathVariable String userId,
                                                         @RequestParam String tenantId,
                                                         @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        Optional<NotificationPreferenceDto> opt = service.get(tenantId, userId);
        return opt.map(dto -> {
            // ETag support (weak example using content hash)
            String etag = Integer.toHexString(dto.hashCode());
            if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
                return new ResponseEntity<NotificationPreferenceDto>(HttpStatus.NOT_MODIFIED);
            }
            return ResponseEntity.ok().header(HttpHeaders.ETAG, etag).body(dto);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping
    public ResponseEntity<NotificationPreferenceDto> put(@PathVariable String userId,
                                                         @RequestParam String tenantId,
                                                         @Valid @RequestBody NotificationPreferenceDto body) {
        body.setUserId(userId);
        body.setTenantId(tenantId);
        NotificationPreferenceDto saved = service.upsert(body);
        return ResponseEntity.ok(saved);
    }
}


