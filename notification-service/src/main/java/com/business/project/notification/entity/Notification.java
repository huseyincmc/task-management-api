package com.business.project.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", uniqueConstraints = @UniqueConstraint(name = "uk_notifications_idempotency_key", columnNames = "idempotency_key"))
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    protected Notification() {
    }

    public Notification(String idempotencyKey, Long taskId, String message, LocalDateTime receivedAt) {
        this.idempotencyKey = idempotencyKey;
        this.taskId = taskId;
        this.message = message;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}
