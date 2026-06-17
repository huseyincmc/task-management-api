package com.business.project.taskmanagement.service;

import com.business.project.taskmanagement.client.NotificationClient;
import com.business.project.taskmanagement.config.RedisChannels;
import com.business.project.taskmanagement.dto.TaskMetaResponse;
import com.business.project.taskmanagement.dto.TaskRequest;
import com.business.project.taskmanagement.dto.TaskResponse;
import com.business.project.taskmanagement.dto.TaskViewCountResponse;
import com.business.project.taskmanagement.entity.Task;
import com.business.project.taskmanagement.exception.TaskNotFoundException;
import com.business.project.taskmanagement.repository.TaskRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final StringRedisTemplate redisTemplate;
    private final NotificationClient notificationClient;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TaskService(
            TaskRepository taskRepository,
            StringRedisTemplate redisTemplate,
            NotificationClient notificationClient
    ) {
        this.taskRepository = taskRepository;
        this.redisTemplate = redisTemplate;
        this.notificationClient = notificationClient;
    }

    @Cacheable(value = "tasks", key = "'all'")
    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        return taskRepository.findAll()
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Cacheable(value = "tasks", key = "#id")
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return TaskResponse.from(getTask(id));
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(request.title(), request.description(), request.completed());
        Task savedTask = taskRepository.save(task);
        redisTemplate.convertAndSend(RedisChannels.TASK_EVENTS, "TASK_CREATED:" + savedTask.getId());
        notificationClient.sendTaskCreatedNotification(savedTask);
        return TaskResponse.from(savedTask);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getTask(id);
        task.update(request.title(), request.description(), request.completed());
        return TaskResponse.from(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public void delete(Long id) {
        Task task = getTask(id);
        taskRepository.delete(task);
        redisTemplate.delete(viewCountKey(id));
        redisTemplate.delete(metaKey(id));
        redisTemplate.convertAndSend(RedisChannels.TASK_EVENTS, "TASK_DELETED:" + id);
    }

    public TaskViewCountResponse incrementViewCount(Long id) {
        getTask(id);
        Long views = redisTemplate.opsForValue().increment(viewCountKey(id));
        redisTemplate.opsForHash().put(metaKey(id), "lastViewedAt", LocalDateTime.now().toString());
        return new TaskViewCountResponse(id, views);
    }

    public TaskViewCountResponse getViewCount(Long id) {
        getTask(id);
        String views = redisTemplate.opsForValue().get(viewCountKey(id));
        return new TaskViewCountResponse(id, views == null ? 0L : Long.parseLong(views));
    }

    public TaskMetaResponse getMeta(Long id) {
        getTask(id);
        Object lastViewedAt = redisTemplate.opsForHash().get(metaKey(id), "lastViewedAt");
        return new TaskMetaResponse(id, lastViewedAt == null ? null : lastViewedAt.toString());
    }

    private Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private String viewCountKey(Long id) {
        return "task:" + id + ":views";
    }

    private String metaKey(Long id) {
        return "task:" + id + ":meta";
    }

}
