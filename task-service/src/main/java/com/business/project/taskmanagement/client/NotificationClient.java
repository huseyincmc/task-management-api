package com.business.project.taskmanagement.client;

import com.business.project.taskmanagement.dto.NotificationRequest;
import com.business.project.taskmanagement.entity.Task;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.TimeUnit;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;
    private final CircuitBreaker notificationCircuitBreaker;
    private final RateLimiter notificationRateLimiter;
    private final String notificationServiceUrl;
    private final int maxAttempts;
    private final long retryDelayMillis;

    public NotificationClient(
            RestClient.Builder restClientBuilder,
            CircuitBreaker notificationCircuitBreaker,
            RateLimiter notificationRateLimiter,
            @Value("${services.notification.url}") String notificationServiceUrl,
            @Value("${services.notification.retry.max-attempts}") int maxAttempts,
            @Value("${services.notification.retry.delay-millis}") long retryDelayMillis
    ) {
        this.restClient = restClientBuilder.build();
        this.notificationCircuitBreaker = notificationCircuitBreaker;
        this.notificationRateLimiter = notificationRateLimiter;
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

        if (!notificationRateLimiter.acquirePermission()) {
            log.warn("Notification rate limit exceeded. Skipping notification for task {}", task.getId());
            return;
        }

        if (!notificationCircuitBreaker.tryAcquirePermission()) {
            log.warn("Notification circuit breaker is {}. Skipping notification for task {}", notificationCircuitBreaker.getState(), task.getId());
            return;
        }

        long startTime = System.nanoTime();
        try {
            sendWithRetry(task.getId(), request);
            notificationCircuitBreaker.onSuccess(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        } catch (RestClientException exception) {
            notificationCircuitBreaker.onError(System.nanoTime() - startTime, TimeUnit.NANOSECONDS, exception);
        }
    }

    private void sendWithRetry(Long taskId, NotificationRequest request) {
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
                    log.warn("Could not send task created notification for task {} after {} attempts: {}", taskId, maxAttempts, exception.getMessage());
                    throw exception;
                }

                log.warn("Could not send task created notification for task {}. Retrying attempt {}/{}", taskId, attempt + 1, maxAttempts);
                sleepBeforeRetry();
            }
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(retryDelayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Notification retry interrupted", exception);
            throw new RestClientException("Notification retry interrupted", exception);
        }
    }
}
