package com.example.taskflow.models;

import java.util.UUID;

public interface AssigneeStatusCount {
    UUID getAssigneeId();
    TaskStatus getStatus();
    Long getCount();
}
