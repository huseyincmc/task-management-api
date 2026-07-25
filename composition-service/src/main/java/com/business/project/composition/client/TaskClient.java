package com.business.project.composition.client;

import com.business.project.composition.dto.TaskResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TaskClient {

    private final RestClient restClient;
    private final String taskServiceUrl;

    public TaskClient(RestClient.Builder restClientBuilder, @Value("${services.task.url}") String taskServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.taskServiceUrl = taskServiceUrl;
    }

    public List<TaskResponse> getTasks() {
        return restClient.get()
                .uri(taskServiceUrl + "/api/tasks")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
