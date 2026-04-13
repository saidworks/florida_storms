/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.batch;

import com.saidworks.florida_storms.models.batch.ProcessedBatch;
import com.saidworks.florida_storms.models.batch.RawBatch;
import com.saidworks.florida_storms.models.domain.DataLine;
import com.saidworks.florida_storms.models.domain.HeaderLine;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service responsible for processing raw batches into parsed and validated data
 */
@Service
@Log4j2
public class BatchProcessorService {

    private final ExecutorService serviceTaskExecutor;

    public BatchProcessorService(
            @Qualifier("serviceTaskExecutor") ExecutorService serviceTaskExecutor) {
        this.serviceTaskExecutor = serviceTaskExecutor;
    }

    /**
     * Processes a raw batch asynchronously using the injected serviceTaskExecutor
     * @param rawBatch The raw batch to process
     * @return CompletableFuture containing the processed batch
     */
    public CompletableFuture<ProcessedBatch> processBatch(@Nonnull RawBatch rawBatch) {
        return CompletableFuture.supplyAsync(
                () -> {
                    long startTime = System.currentTimeMillis();
                    log.debug(
                            "Processing batch {} (lines {}-{})",
                            rawBatch.getBatchId(),
                            rawBatch.getStartLineNumber(),
                            rawBatch.getEndLineNumber());

                    ProcessedBatch.ProcessedBatchBuilder batchBuilder =
                            ProcessedBatch.createEmptyBatch(rawBatch.getBatchId());

                    ProcessedBatch.PartialCyclone currentPartial = null;
                    List<ProcessedBatch.PartialCyclone> partialCyclones = new ArrayList<>();
                    List<String> errors = new ArrayList<>();

                    for (int i = 0; i < rawBatch.getLines().size(); i++) {
                        String line = rawBatch.getLines().get(i);
                        int lineNumber = rawBatch.getStartLineNumber() + i + 1;

                        currentPartial =
                                processPartialCyclone(
                                        rawBatch,
                                        line,
                                        currentPartial,
                                        partialCyclones,
                                        lineNumber,
                                        errors);
                    }

                    // Add last partial
                    if (currentPartial != null) {
                        partialCyclones.add(currentPartial);
                    }

                    long processingTime = System.currentTimeMillis() - startTime;

                    ProcessedBatch result =
                            batchBuilder
                                    .partialCyclones(partialCyclones)
                                    .validationErrors(errors)
                                    .valid(errors.isEmpty())
                                    .processingTimeMs(processingTime)
                                    .build();

                    log.debug(
                            "Completed batch {} in {}ms with {} partial cyclones",
                            rawBatch.getBatchId(),
                            processingTime,
                            partialCyclones.size());

                    return result;
                },
                serviceTaskExecutor);
    }

    /**
     * Parses a single line into the evolving {@code PartialCyclone} structure.
     * - If the line is a header, it finalizes the previous partial (if any) and starts a new one.
     * - If the line is a data line, it adds all track points after 1900 to the current partial.
     *   Landfall detection strategies (L-marker, geo-coordinate, hurricane) are applied
     *   downstream in {@code LandfallFilterService} to support F-REQ-4-a/b/c.
     * Any parsing error is collected into {@code errors} and logged as a warning.
     */
    private static ProcessedBatch.PartialCyclone processPartialCyclone(
            RawBatch rawBatch,
            String line,
            ProcessedBatch.PartialCyclone currentPartial,
            List<ProcessedBatch.PartialCyclone> partialCyclones,
            int lineNumber,
            List<String> errors) {
        try {
            if (line.startsWith("AL")) {
                // Save previous partial if exists
                if (currentPartial != null) {
                    partialCyclones.add(currentPartial);
                }

                // Start new partial cyclone
                HeaderLine header = HeaderLine.parse(line);
                currentPartial =
                        ProcessedBatch.PartialCyclone.builder()
                                .cycloneId(header.getCycloneId())
                                .header(header)
                                .dataLines(new ArrayList<>())
                                .isHeaderPresent(true)
                                .isComplete(false)
                                .build();

            } else {
                // Data line
                if (currentPartial == null) {
                    // Batch starts mid-cyclone, create partial without header
                    currentPartial =
                            ProcessedBatch.PartialCyclone.builder()
                                    .cycloneId("UNKNOWN_" + rawBatch.getBatchId())
                                    .header(null)
                                    .dataLines(new ArrayList<>())
                                    .isHeaderPresent(false)
                                    .isComplete(false)
                                    .build();
                }

                DataLine dataLine = DataLine.parse(line);
                if (dataLine.isAfter1900()) {
                    // Store all track points after 1900; landfall detection
                    // (L-marker, geo-coordinate, hurricane) is applied downstream in
                    // LandfallFilterService. See F-REQ-4-a/b/c.
                    currentPartial.getDataLines().add(dataLine);
                }
            }

        } catch (IllegalArgumentException e) {
            log.warn("Parse error at line {}: {}", lineNumber, e.getMessage());
            errors.add(
                    "Error parsing line %d in batch %d: %s"
                            .formatted(lineNumber, rawBatch.getBatchId(), e.getMessage()));
        }
        return currentPartial;
    }

    /**
     * Validates a processed batch
     * @param batch The batch to validate
     * @return true if valid, false otherwise
     */
    public boolean validateBatch(ProcessedBatch batch) {
        if (!batch.isValid()) {
            log.warn(
                    "Batch {} has parsing errors: {}",
                    batch.getBatchId(),
                    batch.getValidationErrors());
            return false;
        }
        return true;
    }
}
