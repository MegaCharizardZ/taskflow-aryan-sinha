package com.example.taskflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TaskResponse {

    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;

    @JsonProperty("project_id")
    private UUID projectId;

    @JsonProperty("assignee_id")
    private UUID assigneeId;

    @JsonProperty("created_by")
    private UUID createdById;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
