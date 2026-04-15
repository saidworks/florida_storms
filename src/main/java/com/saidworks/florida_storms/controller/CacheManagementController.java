/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cache Admin", description = "Cache management operations")
@RestController
@RequestMapping("/admin/cache")
@Log4j2
public class CacheManagementController {

    private final CacheManager cacheManager;

    public CacheManagementController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Operation(
            summary = "Evict all caches",
            description = "Clears all cached data (cyclones, geocoding)")
    @DeleteMapping
    public ResponseEntity<String> evictAll() {
        cacheManager
                .getCacheNames()
                .forEach(
                        name -> {
                            var cache = cacheManager.getCache(name);
                            if (cache != null) {
                                cache.clear();
                                log.info("Evicted cache: {}", name);
                            }
                        });
        return ResponseEntity.ok("All caches evicted");
    }

    @Operation(summary = "Evict a specific cache by name")
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<String> evictByName(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        cache.clear();
        log.info("Evicted cache: {}", cacheName);
        return ResponseEntity.ok("Cache evicted: " + cacheName);
    }
}
