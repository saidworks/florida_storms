/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.it;

import com.saidworks.florida_storms.FloridaStormsApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = FloridaStormsApplication.class)
public class CucumberSpringConfiguration {
    // Shared Cucumber + Spring Boot test configuration
}
