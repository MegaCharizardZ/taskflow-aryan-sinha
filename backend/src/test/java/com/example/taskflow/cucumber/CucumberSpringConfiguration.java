package com.example.taskflow.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("cucumber")
public class CucumberSpringConfiguration {

    /**
     * @ServiceConnection registers a JdbcConnectionDetails bean directly into
     * Spring Boot's auto-configuration chain. Flyway and Hikari both read from
     * this bean, so the correct container URL is visible at the right time and
     * the flywayInitializer → entityManagerFactory dependency chain is preserved.
     *
     * The container is started once in the static block and shared across all scenarios.
     * Testcontainers cleans it up via a JVM shutdown hook.
     */
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

//    static {
//        POSTGRES.start();
//    }
}
