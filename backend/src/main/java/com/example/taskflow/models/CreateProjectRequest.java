package com.example.taskflow.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 10_000)
    private String description;
}
