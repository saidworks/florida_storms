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
     * Loads cyclone data from all configured HURDAT2 files and splits into raw batches.
     * Each batch respects cyclone boundaries — no cyclone is split across batches.
     *
     * @return combined list of raw batches from all files, ready for processing
     */
    public List<RawBatch> loadBatches() {
        int targetChunkSize = properties.getProcessing().getChunkSize();
        List<String> filePaths = properties.getData().getHurdat2();
        log.info(
                "Loading cyclone data from {} file(s), chunk size: {}",
                filePaths.size(),
                targetChunkSize);

        return CompletableFuture.supplyAsync(
                        () -> {
                            List<RawBatch> allBatches = new ArrayList<>();

                            for (String path : filePaths) {
                                Resource resource = resourceLoader.getResource(path);
                                log.info("Loading file: {}", resource.getFilename());

                                try (BufferedReader reader =
                                        new BufferedReader(
                                                new InputStreamReader(resource.getInputStream()))) {
                                    List<RawBatch> fileBatches =
                                            splitToBatches(reader, targetChunkSize);
                                    allBatches.addAll(fileBatches);

                                    int fileLines =
                                            fileBatches.stream()
                                                    .mapToInt(b -> b.getLines().size())
                                                    .sum();
                                    log.info(
                                            "Loaded {} batches from {} (total lines: {})",
                                            fileBatches.size(),
                                            resource.getFilename(),
                                            fileLines);
                                } catch (IOException e) {
                                    log.error("Error reading file: {}", resource.getFilename(), e);
                                    throw new IoBlockingException(
                                            "Failed to read cyclone data file: " + path, e);
                                }
                            }

                            log.info(
                                    "Total: {} batches from {} file(s)",
                                    allBatches.size(),
                                    filePaths.size());
                            return allBatches;
                        },
                        serviceTaskExecutor)
                .join();
    }
}
