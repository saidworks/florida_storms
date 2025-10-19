/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.helper;

import com.saidworks.florida_storms.config.ExecutorConfigProperties;
import java.util.concurrent.ExecutorService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ConfigurableThreadPool {
    private ConfigurableThreadPool() {
        throw new IllegalStateException("can not not instantiate helper method");
    }

    public static ExecutorService createInstance(ExecutorConfigProperties.Config config) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(
                config.getCorePoolSize()); // Set the core pool size to 5
        threadPoolTaskExecutor.setMaxPoolSize(
                config.getMaxPoolSize()); // Set maximum pool size to 5
        threadPoolTaskExecutor.setQueueCapacity(
                config.getQueueCapacity()); // Set queue capacity, if needed
        threadPoolTaskExecutor.setKeepAliveSeconds(
                config.getKeepAlive()); // Adjust keep-alive time as necessary
        threadPoolTaskExecutor.setThreadNamePrefix(
                config.getPrefix()); // Optional: Prefix for thread names
        threadPoolTaskExecutor.initialize(); // Initialize the executor
        return threadPoolTaskExecutor.getThreadPoolExecutor();
    }
}
