/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.io;

import com.saidworks.florida_storms.models.Cyclone;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Main parser facade that delegates to the multi-threaded batch processing pipeline
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class CycloneDataParser {

    private final CycloneProcessingOrchestrator orchestrator;

    /**
     * Reads cyclone tracking data from the configured file using batch processing
     * This method handles multiple cyclones and processes them in parallel batches
     * @return List of parsed cyclones
     */
    public List<Cyclone> fromFile() throws IOException {
        log.info("Starting cyclone data parsing with batch processing pipeline");
        return orchestrator.processAllCyclones();
    }
}
