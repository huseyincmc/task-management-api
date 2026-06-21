package com.business.project.notification.controller;

import com.business.project.notification.dto.NotificationRequest;
import com.business.project.notification.dto.NotificationResponse;
import com.business.project.notification.service.NotificationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody NotificationRequest request) {
        return notificationService.create(request);
    }

    @GetMapping
    public List<NotificationResponse> getAll() {
        return notificationService.getAll();
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable Long id) {
        return notificationService.getById(id);
    }
}
