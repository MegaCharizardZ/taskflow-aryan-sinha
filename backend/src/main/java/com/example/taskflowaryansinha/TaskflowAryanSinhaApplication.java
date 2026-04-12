package com.example.taskflowaryansinha;

import com.example.taskflowaryansinha.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TaskflowAryanSinhaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskflowAryanSinhaApplication.class, args);
    }

}
