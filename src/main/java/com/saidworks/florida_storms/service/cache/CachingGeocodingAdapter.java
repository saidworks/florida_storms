/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.cache;

import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.service.landfall.GeocodingService;
import com.saidworks.florida_storms.service.port.GeocodingPort;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Caching decorator for {@link GeocodingPort}. Caches area boundary lookups so repeated
 * requests for the same area name hit cache instead of the Nominatim API.
 *
 * <p>Uses manual cache API (not {@code @Cacheable}) because the return type is
 * {@code CompletableFuture<GeoBoundary>} — Spring Cache would cache the Future wrapper,
 * not the resolved value.
 */
@Service
@Primary
@Log4j2
public class CachingGeocodingAdapter implements GeocodingPort {

    private static final String CACHE_NAME = "geocoding";

    private final GeocodingService geocodingService;
    private final CacheManager cacheManager;

    public CachingGeocodingAdapter(GeocodingService geocodingService, CacheManager cacheManager) {
        this.geocodingService = geocodingService;
        this.cacheManager = cacheManager;
    }

    @Override
    public CompletableFuture<GeoBoundary> getAreaBoundaries(String areaName) {
        Cache cache = cacheManager.getCache(CACHE_NAME);

        if (cache != null) {
            GeoBoundary cached = cache.get(areaName, GeoBoundary.class);
            if (cached != null) {
                log.info("Geocoding cache hit for area: {}", areaName);
                return CompletableFuture.completedFuture(cached);
            }
        }

        log.info("Geocoding cache miss for area: {}", areaName);
        return geocodingService
                .getAreaBoundaries(areaName)
                .thenApply(
                        boundary -> {
                            if (cache != null) {
                                cache.put(areaName, boundary);
                            }
                            return boundary;
                        });
    }
}
