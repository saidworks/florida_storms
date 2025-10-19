/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.io;

import com.saidworks.florida_storms.models.DataLine;
import com.saidworks.florida_storms.models.HeaderLine;
import com.saidworks.florida_storms.models.ProcessedBatch;
import com.saidworks.florida_storms.models.RawBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for processing raw batches into parsed and validated data
 */
@Service
@Log4j2
public class BatchProcessorService {

    /**
     * Processes a raw batch asynchronously
     * @param rawBatch The raw batch to process
     * @return CompletableFuture containing the processed batch
     */
    @Async
    public CompletableFuture<ProcessedBatch> processBatch(RawBatch rawBatch) {
        long startTime = System.currentTimeMillis();
        log.debug(
                "Processing batch {} (lines {}-{})",
                rawBatch.getBatchId(),
                rawBatch.getStartLineNumber(),
                rawBatch.getEndLineNumber());

        ProcessedBatch.ProcessedBatchBuilder batchBuilder =
                ProcessedBatch.createEmpty(rawBatch.getBatchId());

        ProcessedBatch.PartialCyclone currentPartial = null;
        List<ProcessedBatch.PartialCyclone> partialCyclones = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rawBatch.getLines().size(); i++) {
            String line = rawBatch.getLines().get(i);
            int lineNumber = rawBatch.getStartLineNumber() + i + 1;

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
                    currentPartial.getDataLines().add(dataLine);
                }

            } catch (Exception e) {
                String error =
                        String.format(
                                "Error parsing line %d: %s - %s", lineNumber, e.getMessage(), line);
                log.warn(error);
                errors.add(error);
            }
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

        return CompletableFuture.completedFuture(result);
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

        for (ProcessedBatch.PartialCyclone partial : batch.getPartialCyclones()) {
            if (partial.isHeaderPresent() && partial.getDataLines().isEmpty()) {
                log.warn(
                        "Batch {}: Cyclone {} has header but no data lines possible missing data"
                                + " verify end result",
                        batch.getBatchId(),
                        partial.getCycloneId());
            }
        }

        return true;
    }
}
