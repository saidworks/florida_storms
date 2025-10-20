/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.batch;

import com.saidworks.florida_storms.config.CycloneProcessingProperties;
import com.saidworks.florida_storms.models.batch.RawBatch;
import com.saidworks.florida_storms.models.exception.IoBlockingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loading the file and splitting it into raw batches using async execution
 * Ensures cyclone headers and their data lines stay together in the same batch
 */
@Service
@Log4j2
public class BatchLoaderService {
    private final CycloneProcessingProperties properties;
    private final ResourceLoader resourceLoader;
    private final ExecutorService serviceTaskExecutor;

    public BatchLoaderService(
            CycloneProcessingProperties properties,
            ResourceLoader resourceLoader,
            @Qualifier("serviceTaskExecutor") ExecutorService serviceTaskExecutor) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.serviceTaskExecutor = serviceTaskExecutor;
    }

    /**
     * Loads the cyclone data file and splits it into raw batches asynchronously
     * Each batch respects cyclone boundaries - no cyclone is split across batches
     * @return List of raw batches ready for processing
     */
    public List<RawBatch> loadBatches() {
        Resource resource = resourceLoader.getResource(properties.getData().getHurdat2());
        int targetChunkSize = properties.getProcessing().getChunkSize();
        AtomicReference<List<RawBatch>> batches = new AtomicReference<>(new ArrayList<>());
        log.info("Loading cyclone data from: {}", resource.getFilename());
        log.info("Target batch chunk size: {}", targetChunkSize);

        return CompletableFuture.supplyAsync(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(resource.getInputStream()))) {
                                batches.set(processCurrentBatch(reader, targetChunkSize));
                            } catch (IOException e) {
                                log.error("Error reading file: {}", resource.getFilename(), e);
                                throw new IoBlockingException(
                                        "Failed to read cyclone data file", e);
                            }
                            int totalLines =
                                    batches.get().stream().mapToInt(b -> b.getLines().size()).sum();
                            log.info(
                                    "Loaded {} batches from file (total lines: {})",
                                    batches.get().size(),
                                    totalLines);

                            // Log batch size distribution
                            batches.get()
                                    .forEach(
                                            batch ->
                                                    log.debug(
                                                            "Batch {}: {} lines",
                                                            batch.getBatchId(),
                                                            batch.getLines().size()));

                            return batches.get();
                        },
                        serviceTaskExecutor)
                .join();
    }

    private List<RawBatch> processCurrentBatch(BufferedReader reader, int targetChunkSize)
            throws IOException {
        List<RawBatch> batches = new ArrayList<>();
        BatchProcessingState state = new BatchProcessingState();
        String line;

        while ((line = reader.readLine()) != null) {
            state.lineNumber++;
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (isHeaderLine(line)) {
                handleHeaderLine(line, state, batches, targetChunkSize);
            } else if (state.inCyclone) {
                state.currentCyclone.add(line);
            } else {
                handleOrphanedLine(line, state);
            }
        }

        finalizeBatches(state, batches);
        return batches;
    }

    private void handleHeaderLine(
            String line, BatchProcessingState state, List<RawBatch> batches, int targetChunkSize) {
        finalizePreviousCyclone(state);

        if (shouldCreateNewBatch(state, targetChunkSize)) {
            createAndAddBatch(state, batches);
        }

        startNewCyclone(line, state);
    }

    private void finalizePreviousCyclone(BatchProcessingState state) {
        if (state.inCyclone && !state.currentCyclone.isEmpty()) {
            state.currentBatch.addAll(state.currentCyclone);
            state.currentCyclone.clear();
        }
    }

    private boolean shouldCreateNewBatch(BatchProcessingState state, int targetChunkSize) {
        return !state.currentBatch.isEmpty() && state.currentBatch.size() >= targetChunkSize;
    }

    private void createAndAddBatch(BatchProcessingState state, List<RawBatch> batches) {
        batches.add(
                createBatch(
                        state.batchId++,
                        state.currentBatch,
                        state.batchStartLine,
                        state.lineNumber - 1));
        state.currentBatch = new ArrayList<>();
        state.batchStartLine = state.lineNumber;
    }

    private void startNewCyclone(String line, BatchProcessingState state) {
        state.currentCyclone.add(line);
        state.inCyclone = true;
    }

    private void handleOrphanedLine(String line, BatchProcessingState state) {
        log.warn("Found orphaned data line at line {}: {}", state.lineNumber, line);
        state.currentBatch.add(line);
    }

    private void finalizeBatches(BatchProcessingState state, List<RawBatch> batches) {
        if (state.inCyclone && !state.currentCyclone.isEmpty()) {
            state.currentBatch.addAll(state.currentCyclone);
        }

        if (!state.currentBatch.isEmpty()) {
            batches.add(
                    createBatch(
                            state.batchId,
                            state.currentBatch,
                            state.batchStartLine,
                            state.lineNumber));
        }
    }

    private static class BatchProcessingState {
        List<String> currentBatch = new ArrayList<>();
        List<String> currentCyclone = new ArrayList<>();
        int lineNumber = 0;
        int batchId = 0;
        int batchStartLine = 0;
        boolean inCyclone = false;
    }

    // ... existing code ...
    /**
     * Determines if a line is a cyclone header line
     * Header lines have fewer commas and contain cyclone metadata
     * Data lines have more commas (typically 20+ fields)
     */
    private boolean isHeaderLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }

        // Count commas - header lines typically have 2-3 commas
        // Data lines have 20+ commas (one before each field)
        long commaCount = line.chars().filter(ch -> ch == ',').count();
        return commaCount <= 3;
    }

    private RawBatch createBatch(int batchId, List<String> lines, int startLine, int endLine) {
        return RawBatch.builder()
                .batchId(batchId)
                .lines(new ArrayList<>(lines))
                .startLineNumber(startLine)
                .endLineNumber(endLine)
                .build();
    }
}
