package com.example.taskflow.service;

import com.example.taskflow.entity.Project;
import com.example.taskflow.entity.Task;
import com.example.taskflow.entity.User;
import com.example.taskflow.models.CreateProjectRequest;
import com.example.taskflow.models.ProjectResponse;
import com.example.taskflow.models.ProjectWithTasksResponse;
import com.example.taskflow.models.TaskResponse;
import com.example.taskflow.models.UpdateProjectRequest;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(UUID userId) {
        List<ProjectResponse> projects = projectRepository.findVisibleToUser(userId).stream()
                .map(this::toResponse)
                .toList();
        log.debug("Listed {} projects for userId={}", projects.size(), userId);
        return projects;
    }

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        User owner = userRepository.getReferenceById(userId);
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();
        ProjectResponse response = toResponse(projectRepository.save(project));
        log.info("Project created: projectId={} name={} ownerId={}", response.getId(), response.getName(), userId);
        return response;
    }

    @Transactional(readOnly = true)
    public ProjectWithTasksResponse getProject(UUID userId, UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));

        if (!projectRepository.isAccessibleByUser(projectId, userId)) {
            log.warn("Access denied: userId={} attempted to access projectId={}", userId, projectId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        List<Task> tasks = taskRepository.findByProjectWithOptionalFilters(projectId, null, null);
        log.debug("Fetched project={} with {} tasks for userId={}", projectId, tasks.size(), userId);
        return toWithTasksResponse(project, tasks);
    }

    @Transactional
    public ProjectResponse updateProject(UUID userId, UUID projectId, UpdateProjectRequest request) {
        Project project = findAsOwnerOrThrow(projectId, userId);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        ProjectResponse response = toResponse(projectRepository.save(project));
        log.info("Project updated: projectId={} by userId={}", projectId, userId);
        return response;
    }

    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        Project project = findAsOwnerOrThrow(projectId, userId);
        taskRepository.deleteByProject_Id(projectId);
        projectRepository.delete(project);
        log.info("Project deleted: projectId={} by userId={}", projectId, userId);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Loads a project the user owns. Throws 404 if the project doesn't exist, 403 if the user is not the owner.
     */
    private Project findAsOwnerOrThrow(UUID projectId, UUID userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        }
        return projectRepository.findByIdAndOwner_Id(projectId, userId)
                .orElseThrow(() -> {
                    log.warn("Access denied: userId={} is not the owner of projectId={}", userId, projectId);
                    return new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
                });
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .ownerId(p.getOwner().getId())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private ProjectWithTasksResponse toWithTasksResponse(Project p, List<Task> tasks) {
        return ProjectWithTasksResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .ownerId(p.getOwner().getId())
                .createdAt(p.getCreatedAt())
                .tasks(tasks.stream().map(this::toTaskResponse).toList())
                .build();
    }

    private TaskResponse toTaskResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .projectId(t.getProject().getId())
                .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                .createdById(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null)
                .dueDate(t.getDueDate())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
