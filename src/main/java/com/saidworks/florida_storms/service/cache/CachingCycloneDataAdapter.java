/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.cache;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.service.batch.CycloneProcessingOrchestrator;
import com.saidworks.florida_storms.service.port.CycloneDataPort;
import java.io.IOException;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Caching decorator for {@link CycloneDataPort}. Caches the full cyclone dataset
 * so the 3-phase batch pipeline only runs on cache misses.
 *
 * <p>Marked {@code @Primary} so all consumers (controllers, filter services) automatically
 * receive cached data. Injects the concrete {@link CycloneProcessingOrchestrator} (not the
 * interface) to avoid circular bean resolution.
 */
@Service
@Primary
@Log4j2
public class CachingCycloneDataAdapter implements CycloneDataPort {

    private final CycloneProcessingOrchestrator orchestrator;

    public CachingCycloneDataAdapter(CycloneProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    @Cacheable(value = "cyclones", key = "'allCyclones'")
    public List<Cyclone> processAllCyclones() throws IOException {
        log.info("Cache miss — running full cyclone processing pipeline");
        return orchestrator.processAllCyclones();
    }
}
