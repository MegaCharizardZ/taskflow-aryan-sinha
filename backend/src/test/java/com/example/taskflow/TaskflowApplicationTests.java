package com.example.taskflow;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires a real database — integration tests run via CucumberTests instead")
@SpringBootTest
class TaskflowApplicationTests {

    @Test
    void contextLoads() {
    }

}
