package com.business.project.taskmanagement.client;

import com.business.project.taskmanagement.dto.NotificationRequest;
import com.business.project.taskmanagement.entity.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;
    private final String notificationServiceUrl;
    private final int maxAttempts;
    private final long retryDelayMillis;

    public NotificationClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.notification.url}") String notificationServiceUrl,
            @Value("${services.notification.retry.max-attempts}") int maxAttempts,
            @Value("${services.notification.retry.delay-millis}") long retryDelayMillis
    ) {
        this.restClient = restClientBuilder.build();
        this.notificationServiceUrl = notificationServiceUrl;
        this.maxAttempts = maxAttempts;
        this.retryDelayMillis = retryDelayMillis;
    }

    public void sendTaskCreatedNotification(Task task) {
        NotificationRequest request = new NotificationRequest(
                "TASK_CREATED:" + task.getId(),
                task.getId(),
                "Task created: " + task.getTitle()
        );

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                restClient.post()
                        .uri(notificationServiceUrl + "/api/notifications")
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (RestClientException exception) {
                if (attempt == maxAttempts) {
                    log.warn("Could not send task created notification for task {} after {} attempts", task.getId(), maxAttempts, exception);
                    return;
                }

                log.warn("Could not send task created notification for task {}. Retrying attempt {}/{}", task.getId(), attempt + 1, maxAttempts);
                if (!sleepBeforeRetry()) {
                    return;
                }
            }
        }
    }

    private boolean sleepBeforeRetry() {
        try {
            Thread.sleep(retryDelayMillis);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Notification retry interrupted", exception);
            return false;
        }
    }
}
