package com.example.taskflow.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskPriority {
    LOW("LOW"), MEDIUM("MEDIUM"), HIGH("HIGH");

    private final String value;

}