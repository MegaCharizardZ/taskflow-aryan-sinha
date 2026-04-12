package com.example.taskflowaryansinha.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectResponse {

    private UUID id;
    private String name;
    private String description;

    @JsonProperty("owner_id")
    private UUID ownerId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
