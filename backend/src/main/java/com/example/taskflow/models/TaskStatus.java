package com.example.taskflow.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    TODO, IN_PROGRESS, DONE;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static TaskStatus fromValue(String value) {
        return TaskStatus.valueOf(value);
    }
}
