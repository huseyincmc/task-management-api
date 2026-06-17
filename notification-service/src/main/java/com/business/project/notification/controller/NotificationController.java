package com.business.project.notification.controller;

import com.business.project.notification.dto.NotificationRequest;
import com.business.project.notification.dto.NotificationResponse;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final Set<String> processedIdempotencyKeys = ConcurrentHashMap.newKeySet();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody NotificationRequest request) {
        if (!processedIdempotencyKeys.add(request.idempotencyKey())) {
            log.info("Duplicate notification ignored: {}", request.idempotencyKey());
            return new NotificationResponse(request.taskId(), request.message(), LocalDateTime.now());
        }

        log.info("Notification received for task {}: {}", request.taskId(), request.message());
        return new NotificationResponse(request.taskId(), request.message(), LocalDateTime.now());
    }
}
