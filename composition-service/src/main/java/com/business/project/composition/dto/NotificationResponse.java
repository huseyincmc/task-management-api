package com.business.project.composition.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String idempotencyKey,
        Long taskId,
        String message,
        LocalDateTime receivedAt
) {
}
