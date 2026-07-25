package com.business.project.composition.controller;

import com.business.project.composition.dto.TaskSummaryResponse;
import com.business.project.composition.service.TaskSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task-summaries")
public class TaskSummaryController {

    private final TaskSummaryService taskSummaryService;

    public TaskSummaryController(TaskSummaryService taskSummaryService) {
        this.taskSummaryService = taskSummaryService;
    }

    @GetMapping
    public List<TaskSummaryResponse> getTaskSummaries() {
        return taskSummaryService.getTaskSummaries();
    }
}
