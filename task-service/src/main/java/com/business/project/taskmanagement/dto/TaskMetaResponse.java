package com.business.project.taskmanagement.dto;

public record TaskMetaResponse(
        Long taskId,
        String lastViewedAt
) {
}
