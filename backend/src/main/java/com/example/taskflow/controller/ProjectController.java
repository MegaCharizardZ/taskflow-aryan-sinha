package com.example.taskflow.controller;

import com.example.taskflow.models.CreateProjectRequest;
import com.example.taskflow.models.ProjectResponse;
import com.example.taskflow.models.ProjectWithTasksResponse;
import com.example.taskflow.models.UpdateProjectRequest;
import com.example.taskflow.service.ProjectService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Projects API. Mutations on a project (PATCH, DELETE) are restricted to the project owner in the service layer.
 * Listing returns projects the current user owns or has tasks in.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects() {
        UUID userId = currentUserId();
        log.debug("User {} listing projects", userId);
        return ResponseEntity.ok(projectService.listProjects(userId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        UUID userId = currentUserId();
        log.info("User {} creating project name={}", userId, request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectWithTasksResponse> getProject(@PathVariable UUID id) {
        UUID userId = currentUserId();
        log.debug("User {} fetching project={}", userId, id);
        return ResponseEntity.ok(projectService.getProject(userId, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        UUID userId = currentUserId();
        log.info("User {} updating project={}", userId, id);
        return ResponseEntity.ok(projectService.updateProject(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        UUID userId = currentUserId();
        log.info("User {} deleting project={}", userId, id);
        projectService.deleteProject(userId, id);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
