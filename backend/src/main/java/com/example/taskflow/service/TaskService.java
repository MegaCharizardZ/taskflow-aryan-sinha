package com.example.taskflow.service;

import com.example.taskflow.entity.Project;
import com.example.taskflow.entity.Task;
import com.example.taskflow.entity.User;
import com.example.taskflow.models.AssigneeStatusCount;
import com.example.taskflow.models.AssigneeTaskStats;
import com.example.taskflow.models.CreateTaskRequest;
import com.example.taskflow.models.TaskPriority;
import com.example.taskflow.models.TaskResponse;
import com.example.taskflow.models.TaskStatus;
import com.example.taskflow.models.UpdateTaskRequest;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(UUID userId, UUID projectId, TaskStatus status, UUID assigneeId) {
        requireProjectAccess(projectId, userId);
        List<TaskResponse> tasks = taskRepository.findByProjectWithOptionalFilters(projectId, status, assigneeId).stream()
                .map(this::toResponse)
                .toList();
        log.debug("Listed {} tasks for projectId={} userId={} status={} assignee={}", tasks.size(), projectId, userId, status, assigneeId);
        return tasks;
    }

    @Transactional
    public TaskResponse createTask(UUID userId, UUID projectId, CreateTaskRequest request) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        }

        Project project = projectRepository.getReferenceById(projectId);
        User creator = userRepository.getReferenceById(userId);
        User assignee = request.getAssignee() != null
                ? userRepository.findById(request.getAssignee())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"))
                : null;

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .taskStatus(request.getStatus() != null ? request.getStatus().getValue() : com.example.taskflow.models.TaskStatus.TODO.getValue())
                .taskPriority(request.getPriority() != null ? request.getPriority().getValue() : com.example.taskflow.models.TaskPriority.LOW.getValue())
                .project(project)
                .assignee(assignee)
                .createdBy(creator)
                .dueDate(request.getDueDate())
                .build();

        TaskResponse response = toResponse(taskRepository.save(task));
        log.info("Task created: taskId={} title={} projectId={} createdBy={} assignee={}",
                response.getId(), response.getTitle(), projectId, userId,
                assignee != null ? assignee.getId() : null);
        return response;
    }

    @Transactional
    public TaskResponse updateTask(UUID userId, UUID taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));

        boolean isCreator  = task.getCreatedBy() != null && userId.equals(task.getCreatedBy().getId());
        boolean isAssignee = task.getAssignee()  != null && userId.equals(task.getAssignee().getId());
        if (!isCreator && !isAssignee) {
            log.warn("Access denied: userId={} attempted to update taskId={}", userId, taskId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setTaskStatus(request.getStatus().getValue());
        }
        if (request.getPriority() != null) {
            task.setTaskPriority(request.getPriority().getValue());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getAssignee() != null) {
            User assignee = userRepository.findById(request.getAssignee())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
            task.setAssignee(assignee);
        }

        TaskResponse response = toResponse(taskRepository.save(task));
        log.info("Task updated: taskId={} by userId={}", taskId, userId);
        return response;
    }

    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        }

        boolean isProjectOwner = taskRepository.existsByIdAndProject_Owner_Id(taskId, userId);
        boolean isTaskCreator = taskRepository.existsByIdAndCreatedBy_Id(taskId, userId);

        if (!isProjectOwner && !isTaskCreator) {
            log.warn("Access denied: userId={} attempted to delete taskId={}", userId, taskId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        taskRepository.deleteById(taskId);
        log.info("Task deleted: taskId={} by userId={}", taskId, userId);
    }

    @Transactional(readOnly = true)
    public List<AssigneeTaskStats> getProjectStats(UUID userId, UUID projectId) {
        requireProjectAccess(projectId, userId);

        Map<UUID, Map<TaskStatus, Long>> grouped = new LinkedHashMap<>();
        for (AssigneeStatusCount row : taskRepository.countByAssigneeAndStatusForProject(projectId)) {
            grouped.computeIfAbsent(row.getAssigneeId(), k -> new LinkedHashMap<>()).put(row.getStatus(), row.getCount());
        }

        return grouped.entrySet().stream()
                .map(e -> AssigneeTaskStats.builder()
                        .assignee(e.getKey())
                        .tasks(e.getValue())
                        .build())
                .toList();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Throws 404 if the project doesn't exist, 403 if the user has no access to it.
     */
    private void requireProjectAccess(UUID projectId, UUID userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        }
        if (!projectRepository.isAccessibleByUser(projectId, userId)) {
            log.warn("Access denied: userId={} has no access to projectId={}", userId, projectId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    private TaskResponse toResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(TaskStatus.valueOf(t.getTaskStatus()))
                .priority(TaskPriority.valueOf(t.getTaskPriority()))
                .projectId(t.getProject().getId())
                .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                .createdById(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null)
                .dueDate(t.getDueDate())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
