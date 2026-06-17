package com.business.project.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotBlank String idempotencyKey,
        @NotNull Long taskId,
        @NotBlank String message
) {
}
