/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.batch;

import static com.saidworks.florida_storms.helper.RawBatchAssembler.splitToBatches;

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

@Log4j2
@Service
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
                                // Delegate batch preparation to helper
                                batches.set(splitToBatches(reader, targetChunkSize));
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
}
