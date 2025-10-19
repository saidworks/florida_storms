/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.io;

import com.saidworks.florida_storms.models.Cyclone;
import com.saidworks.florida_storms.models.ProcessedBatch;
import com.saidworks.florida_storms.models.RawBatch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the three-phase cyclone data processing pipeline:
 * 1. Load file into raw batches
 * 2. Process and validate batches in parallel
 * 3. Merge batches into complete cyclones
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CycloneProcessingOrchestrator {

    private final BatchLoaderService batchLoaderService;
    private final BatchProcessorService batchProcessorService;
    private final BatchMergerService batchMergerService;

    /**
     * Executes the complete processing pipeline
     * @return List of processed cyclones
     */
    public List<Cyclone> processAllCyclones() throws IOException {
        log.info("=== Starting Cyclone Data Processing Pipeline ===");
        long pipelineStart = System.currentTimeMillis();

        // Phase 1: Load batches
        log.info("Phase 1: Loading raw batches...");
        long phase1Start = System.currentTimeMillis();
        List<RawBatch> rawBatches = batchLoaderService.loadBatches();
        long phase1Duration = System.currentTimeMillis() - phase1Start;
        log.info("Phase 1 completed in {}ms", phase1Duration);

        // Phase 2: Process batches in parallel
        log.info("Phase 2: Processing {} batches in parallel...", rawBatches.size());
        long phase2Start = System.currentTimeMillis();
        List<ProcessedBatch> processedBatches = processBatchesInParallel(rawBatches);
        long phase2Duration = System.currentTimeMillis() - phase2Start;
        log.info("Phase 2 completed in {}ms", phase2Duration);

        // Validate processed batches
        log.info("Validating processed batches...");
        int validBatches = 0;
        int invalidBatches = 0;
        for (ProcessedBatch batch : processedBatches) {
            if (batchProcessorService.validateBatch(batch)) {
                validBatches++;
            } else {
                invalidBatches++;
            }
        }
        log.info("Validation: {} valid, {} invalid batches", validBatches, invalidBatches);

        // Phase 3: Merge batches
        log.info("Phase 3: Merging batches into complete cyclones...");
        long phase3Start = System.currentTimeMillis();
        List<Cyclone> cyclones = batchMergerService.mergeBatches(processedBatches);
        long phase3Duration = System.currentTimeMillis() - phase3Start;
        log.info("Phase 3 completed in {}ms", phase3Duration);

        long totalDuration = System.currentTimeMillis() - pipelineStart;
        log.info(
                "=== Pipeline completed in {}ms (Load: {}ms, Process: {}ms, Merge: {}ms) ===",
                totalDuration,
                phase1Duration,
                phase2Duration,
                phase3Duration);
        log.info("Total cyclones processed: {}", cyclones.size());

        return cyclones;
    }

    /**
     * Processes all raw batches in parallel using async threads
     * @param rawBatches List of raw batches to process
     * @return List of processed batches in original order
     */
    private List<ProcessedBatch> processBatchesInParallel(List<RawBatch> rawBatches) {
        List<CompletableFuture<ProcessedBatch>> futures = new ArrayList<>();

        // Submit all batches for async processing
        for (RawBatch rawBatch : rawBatches) {
            CompletableFuture<ProcessedBatch> future = batchProcessorService.processBatch(rawBatch);
            futures.add(future);
        }

        // Wait for all to complete and collect results
        CompletableFuture<Void> allFutures =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Block until all are done
        allFutures.join();

        // Collect results in order
        List<ProcessedBatch> processedBatches = new ArrayList<>();
        for (CompletableFuture<ProcessedBatch> future : futures) {
            try {
                processedBatches.add(future.get());
            } catch (Exception e) {
                log.error("Error getting processed batch result", e);
            }
        }

        // Sort by batch ID to maintain order
        processedBatches.sort((a, b) -> Integer.compare(a.getBatchId(), b.getBatchId()));

        return processedBatches;
    }
}
