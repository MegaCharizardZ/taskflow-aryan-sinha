package com.example.taskflow.cucumber.hooks;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs before every scenario to wipe all application data.
 * This also removes any rows inserted by Flyway's V3 seed migration,
 * giving each scenario a guaranteed clean starting state.
 * ORDER = 1 ensures this runs before any step-level @Before hooks.
 */
public class DatabaseCleanupHook {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before(order = 1)
    public void cleanDatabase() {
        // tasks first (references projects + users), then projects (references users), then users
        jdbcTemplate.execute("TRUNCATE TABLE tasks, projects, users CASCADE");
    }
}
