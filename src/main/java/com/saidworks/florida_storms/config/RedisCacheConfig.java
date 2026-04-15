/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Redis-backed cache configuration for the prod profile.
 * Serializes cached values as JSON so they are debuggable and version-tolerant.
 */
@Configuration
@Profile("prod")
@EnableCaching
public class RedisCacheConfig {

    public static final String CYCLONE_CACHE = "cyclones";
    public static final String GEOCODING_CACHE = "geocoding";

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory, CacheProperties cacheProperties) {

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(cacheProperties.getTtl())
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(CYCLONE_CACHE, defaultConfig)
                .withCacheConfiguration(GEOCODING_CACHE, defaultConfig)
                .build();
    }
}
