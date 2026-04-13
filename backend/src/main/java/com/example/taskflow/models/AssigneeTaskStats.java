package com.example.taskflow.models;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AssigneeTaskStats {

    private UUID assignee;
    private Map<TaskStatus, Long> tasks;
}