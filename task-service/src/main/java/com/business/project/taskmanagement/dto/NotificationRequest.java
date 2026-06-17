package com.business.project.taskmanagement.dto;

public record NotificationRequest(
        String idempotencyKey,
        Long taskId,
        String message
) {
}
