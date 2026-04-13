/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * In-memory cache for dev profile and tests. No Redis dependency required.
 */
@Configuration
@Profile("!prod")
@EnableCaching
public class InMemoryCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(
                List.of(
                        new ConcurrentMapCache(RedisCacheConfig.CYCLONE_CACHE),
                        new ConcurrentMapCache(RedisCacheConfig.GEOCODING_CACHE)));
        return manager;
    }
}
