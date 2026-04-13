package com.example.taskflow.controller;

import com.example.taskflow.models.AssigneeTaskStats;
import com.example.taskflow.models.CreateTaskRequest;
import com.example.taskflow.models.TaskResponse;
import com.example.taskflow.models.TaskStatus;
import com.example.taskflow.models.UpdateTaskRequest;
import com.example.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Tasks API under projects plus task-scoped update/delete.
 * DELETE /tasks/{id}: allowed only for the project owner or the user who created the task.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<List<AssigneeTaskStats>> getProjectStats(@PathVariable UUID projectId) {
        UUID userId = currentUserId();
        log.debug("User {} fetching stats for project={}", userId, projectId);
        return ResponseEntity.ok(taskService.getProjectStats(userId, projectId));
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> listTasks(
            @PathVariable UUID projectId,
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "assignee", required = false) UUID assignee
    ) {
        UUID userId = currentUserId();
        log.debug("User {} listing tasks for project={} status={} assignee={}", userId, projectId, status, assignee);
        return ResponseEntity.ok(taskService.listTasks(userId, projectId, status, assignee));
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        UUID userId = currentUserId();
        log.info("User {} creating task in project={}", userId, projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(userId, projectId, request));
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        UUID userId = currentUserId();
        log.info("User {} updating task={}", userId, id);
        return ResponseEntity.ok(taskService.updateTask(userId, id, request));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        UUID userId = currentUserId();
        log.info("User {} deleting task={}", userId, id);
        taskService.deleteTask(userId, id);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
