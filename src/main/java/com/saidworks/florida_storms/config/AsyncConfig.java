/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration to enable asynchronous processing
 */
@Configuration
@EnableAsync
@Log4j2
public class AsyncConfig {

    public AsyncConfig() {
        log.info("Async processing enabled for batch processing");
    }
}
