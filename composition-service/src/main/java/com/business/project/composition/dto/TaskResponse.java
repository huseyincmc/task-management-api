package com.business.project.composition.dto;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
