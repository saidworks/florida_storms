/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("executors")
@Data
public class ExecutorConfigProperties {
    private IOBlocking ioBlocking = new IOBlocking();
    private Service service = new Service();
    private Controller controller = new Controller();

    @Data
    public static class IOBlocking {
        private Config config = new Config();
    }

    @Data
    public static class Service {
        private Config config = new Config();
    }

    @Data
    public static class Controller {
        private Config config = new Config();
    }

    @Data
    public static class Config {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private int keepAlive;
        private String prefix;
    }
}
