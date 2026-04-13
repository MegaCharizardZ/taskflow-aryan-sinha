package com.example.taskflow;

import com.example.taskflow.config.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TaskflowApplication {

    @Value("${app.debug}")
    private String jwtSecret;

    public static void main(String[] args) {
        SpringApplication.run(TaskflowApplication.class, args);
    }

}
