/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.io;

import com.saidworks.florida_storms.models.Cyclone;
import com.saidworks.florida_storms.models.ProcessedBatch;
import java.util.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Service responsible for merging processed batches into complete cyclones
 */
@Service
@Log4j2
public class BatchMergerService {

    /**
     * Merges processed batches into complete cyclones
     * @param processedBatches List of processed batches in order
     * @return List of complete cyclones
     */
    public List<Cyclone> mergeBatches(List<ProcessedBatch> processedBatches) {
        log.info("Merging {} processed batches", processedBatches.size());

        // Map to accumulate cyclone data by cyclone ID
        Map<String, Cyclone> cycloneMap = new LinkedHashMap<>();

        // Track orphaned data lines (data lines without a header in previous batches)
        List<ProcessedBatch.PartialCyclone> orphanedPartials = new ArrayList<>();

        for (ProcessedBatch batch : processedBatches) {
            for (ProcessedBatch.PartialCyclone partial : batch.getPartialCyclones()) {

                if (partial.isHeaderPresent()) {
                    // This partial has a header, start or update cyclone
                    Cyclone cyclone =
                            cycloneMap.computeIfAbsent(
                                    partial.getCycloneId(), _ -> new Cyclone(partial.getHeader()));

                    cyclone.getDataLines().addAll(partial.getDataLines());

                } else {
                    // No header present - this is continuation from previous batch
                    // Try to attach to the last cyclone in map
                    if (!cycloneMap.isEmpty()) {
                        Cyclone lastCyclone = getLastCyclone(cycloneMap);
                        lastCyclone.getDataLines().addAll(partial.getDataLines());
                        log.debug(
                                "Attached {} orphaned data lines to cyclone {}",
                                partial.getDataLines().size(),
                                lastCyclone.getHeader().getCycloneId());
                    } else {
                        // No cyclone to attach to yet, save for later
                        orphanedPartials.add(partial);
                        log.warn(
                                "Found orphaned data lines in batch {} without any previous"
                                        + " cyclone",
                                batch.getBatchId());
                    }
                }
            }
        }

        // Handle any remaining orphaned partials
        if (!orphanedPartials.isEmpty()) {
            log.warn("Found {} orphaned partials that couldn't be merged", orphanedPartials.size());
        }

        List<Cyclone> cyclones = new ArrayList<>(cycloneMap.values());

        // Validate merged cyclones
        int completeCount = 0;
        int incompleteCount = 0;

        for (Cyclone cyclone : cyclones) {
            if (cyclone.isDataCompletePerHeader()) {
                completeCount++;
            } else {
                incompleteCount++;
                log.debug(
                        "Cyclone {} has incomplete data: expected {} entries, found {}",
                        cyclone.getHeader().getCycloneId(),
                        cyclone.getHeader().getEntriesCount(),
                        cyclone.getDataLines().size());
            }
        }

        log.info(
                "Merged into {} cyclones ({} complete, {} incomplete)",
                cyclones.size(),
                completeCount,
                incompleteCount);

        return cyclones;
    }

    private Cyclone getLastCyclone(Map<String, Cyclone> map) {
        return map.values().stream().reduce((_, second) -> second).orElse(null);
    }
}
