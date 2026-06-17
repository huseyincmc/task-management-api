package com.business.project.taskmanagement.repository;

import com.business.project.taskmanagement.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
