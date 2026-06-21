package com.business.project.notification.service;

import com.business.project.notification.dto.NotificationRequest;
import com.business.project.notification.dto.NotificationResponse;
import com.business.project.notification.entity.Notification;
import com.business.project.notification.exception.NotificationNotFoundException;
import com.business.project.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        return notificationRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(notification -> {
                    log.info("Duplicate notification ignored: {}", request.idempotencyKey());
                    return toResponse(notification);
                })
                .orElseGet(() -> saveNewNotification(request));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(Long id) {
        return notificationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    private NotificationResponse saveNewNotification(NotificationRequest request) {
        try {
            Notification notification = new Notification(
                    request.idempotencyKey(),
                    request.taskId(),
                    request.message(),
                    LocalDateTime.now()
            );

            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification saved for task {}: {}", request.taskId(), request.message());
            return toResponse(savedNotification);
        } catch (DataIntegrityViolationException exception) {
            log.info("Duplicate notification ignored after database constraint: {}", request.idempotencyKey());
            return notificationRepository.findByIdempotencyKey(request.idempotencyKey())
                    .map(this::toResponse)
                    .orElseThrow(() -> exception);
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getIdempotencyKey(),
                notification.getTaskId(),
                notification.getMessage(),
                notification.getReceivedAt()
        );
    }
}
