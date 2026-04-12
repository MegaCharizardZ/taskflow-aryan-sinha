package com.example.taskflowaryansinha.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 10_000)
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private UUID assignee;

    @JsonProperty("due_date")
    private LocalDate dueDate;
}
