package com.business.project.composition.dto;

import java.time.LocalDateTime;

public record TaskSummaryResponse(
        Long taskId,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String notificationMessage,
        LocalDateTime notificationReceivedAt
) {
}
