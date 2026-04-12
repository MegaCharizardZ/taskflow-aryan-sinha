package com.example.taskflowaryansinha.controller;

import com.example.taskflowaryansinha.models.CreateTaskRequest;
import com.example.taskflowaryansinha.models.TaskStatus;
import com.example.taskflowaryansinha.models.UpdateTaskRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Tasks API under projects plus task-scoped update/delete.
 * <p>
 * DELETE {@code /tasks/{id}}: allowed only for the project owner or the user who created the task (enforced when services persist {@code createdBy} / equivalent).
 */
@Validated
@RestController
public class TaskController {

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Void> listTasks(
            @PathVariable UUID projectId,
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "assignee", required = false) UUID assignee
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Void> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<Void> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
