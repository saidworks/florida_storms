/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cyclone")
public class CycloneProcessingProperties {
    private Processing processing = new Processing();
    private Data data = new Data();

    @lombok.Data
    public static class Processing {
        private int chunkSize;
    }

    @lombok.Data
    public static class Data {
        private String hurdat2;
    }
}
