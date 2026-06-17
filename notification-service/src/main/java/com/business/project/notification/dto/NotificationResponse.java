package com.business.project.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long taskId,
        String message,
        LocalDateTime receivedAt
) {
}
