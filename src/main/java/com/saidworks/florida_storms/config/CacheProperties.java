/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cyclone.cache")
public class CacheProperties {
    /** Time-to-live for cached data. Default 24 hours. */
    private Duration ttl = Duration.ofHours(24);
}
