/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import com.saidworks.florida_storms.helper.ConfigurableThreadPool;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for services and controller threads
 */
@Configuration
@Log4j2
public class AsyncConfig {
    private final ExecutorConfigProperties executorConfigProperties;

    public AsyncConfig(ExecutorConfigProperties executorConfigProperties) {
        this.executorConfigProperties = executorConfigProperties;
    }

    @Bean(name = "serviceTaskExecutor", destroyMethod = "shutdown")
    public ExecutorService serviceTaskExecutor() {
        ExecutorConfigProperties.Config serviceThreadConfig =
                executorConfigProperties.getService().getConfig();
        return ConfigurableThreadPool.createInstance(serviceThreadConfig);
    }

    @Bean(name = "controllerTaskExecutor", destroyMethod = "shutdown")
    public ExecutorService controllerTaskExecutor() {
        ExecutorConfigProperties.Config controllerThreadConfig =
                executorConfigProperties.getIoBlocking().getConfig();
        return ConfigurableThreadPool.createInstance(controllerThreadConfig);
    }

    @Bean(name = "ioBlockingTaskExecutor", destroyMethod = "shutdown")
    public ExecutorService ioBlockingTaskExecutor() {
        ExecutorConfigProperties.Config databaseThreadConfig =
                executorConfigProperties.getIoBlocking().getConfig();
        return ConfigurableThreadPool.createInstance(databaseThreadConfig);
    }
}
