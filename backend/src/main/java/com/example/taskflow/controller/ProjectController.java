package com.example.taskflow.controller;

import com.example.taskflow.models.CreateProjectRequest;
import com.example.taskflow.models.ProjectResponse;
import com.example.taskflow.models.ProjectWithTasksResponse;
import com.example.taskflow.models.UpdateProjectRequest;
import com.example.taskflow.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Projects API. Mutations on a project (PATCH, DELETE) are restricted to the project owner in the service layer.
 * Listing returns projects the current user owns or has tasks in.
 */
@Validated
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects() {
        return ResponseEntity.ok(projectService.listProjects(currentUserId()));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(currentUserId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectWithTasksResponse> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(currentUserId(), id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProject(currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(currentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
