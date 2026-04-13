package com.example.taskflow.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPriority {
    LOW, MEDIUM, HIGH;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static TaskPriority fromValue(String value) {
        return TaskPriority.valueOf(value);
    }
}