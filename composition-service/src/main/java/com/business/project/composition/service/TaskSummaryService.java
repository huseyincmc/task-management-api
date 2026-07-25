package com.business.project.composition.service;

import com.business.project.composition.client.NotificationClient;
import com.business.project.composition.client.TaskClient;
import com.business.project.composition.dto.NotificationResponse;
import com.business.project.composition.dto.TaskResponse;
import com.business.project.composition.dto.TaskSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TaskSummaryService {

    private final TaskClient taskClient;
    private final NotificationClient notificationClient;

    public TaskSummaryService(TaskClient taskClient, NotificationClient notificationClient) {
        this.taskClient = taskClient;
        this.notificationClient = notificationClient;
    }

    public List<TaskSummaryResponse> getTaskSummaries() {
        List<TaskResponse> tasks = taskClient.getTasks();
        Map<Long, NotificationResponse> latestNotificationByTaskId = notificationClient.getNotifications().stream()
                .collect(Collectors.toMap(
                        NotificationResponse::taskId,
                        Function.identity(),
                        TaskSummaryService::latestNotification
                ));

        return tasks.stream()
                .map(task -> toSummary(task, latestNotificationByTaskId.get(task.id())))
                .toList();
    }

    private static NotificationResponse latestNotification(
            NotificationResponse first,
            NotificationResponse second
    ) {
        return Comparator.comparing(NotificationResponse::receivedAt)
                .compare(first, second) >= 0 ? first : second;
    }

    private static TaskSummaryResponse toSummary(TaskResponse task, NotificationResponse notification) {
        return new TaskSummaryResponse(
                task.id(),
                task.title(),
                task.description(),
                task.completed(),
                task.createdAt(),
                task.updatedAt(),
                notification == null ? null : notification.message(),
                notification == null ? null : notification.receivedAt()
        );
    }
}
