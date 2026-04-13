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
    private Hurricane hurricane = new Hurricane();

    @lombok.Data
    public static class Processing {
        private int chunkSize;
    }

    @lombok.Data
    public static class Data {
        private String hurdat2;
    }

    /**
     * Thresholds used by F-REQ-4-b hurricane classification filtering.
     * A storm qualifies as a hurricane when wind speed >= minWindSpeedKnots
     * and (if present) central pressure <= maxCentralPressureMbar.
     */
    @lombok.Data
    public static class Hurricane {
        /** Saffir-Simpson Category 1 lower bound in knots. */
        private int minWindSpeedKnots = 64;

        /** Typical sea-level pressure at hurricane intensity (mbar). */
        private int maxCentralPressureMbar = 1000;
    }
}
