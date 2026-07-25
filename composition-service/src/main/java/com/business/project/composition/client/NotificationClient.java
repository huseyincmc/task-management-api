package com.business.project.composition.client;

import com.business.project.composition.dto.NotificationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class NotificationClient {

    private final RestClient restClient;
    private final String notificationServiceUrl;

    public NotificationClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.notification.url}") String notificationServiceUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public List<NotificationResponse> getNotifications() {
        return restClient.get()
                .uri(notificationServiceUrl + "/api/notifications")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
