/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.saidworks.florida_storms.models.batch.ProcessedBatch;
import com.saidworks.florida_storms.models.batch.RawBatch;
import com.saidworks.florida_storms.models.domain.DataLine;
import com.saidworks.florida_storms.models.domain.HeaderLine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Log4j2
class BatchProcessorServiceTest {
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private final BatchProcessorService batchProcessorService =
            new BatchProcessorService(executorService);

    // Test method for processBatch with valid data
    @Test
    void testProcessBatch_ValidData() {
        RawBatch rawBatch =
                RawBatch.builder()
                        .batchId(1)
                        .lines(
                                List.of(
                                        "AL041851,            UNNAMED,     49,",
                                        "19510816, 0000, L , TS, 13.4N,  48.0W,  40, -999, -999,"
                                                + " -999, -999, -999, -999, -999, -999, -999, -999,"
                                                + " -999, -999, -999",
                                        "19510816, 0600, L , TS, 13.7N,  49.5W,  40, -999, -999,"
                                                + " -999, -999, -999, -999, -999, -999, -999, -999,"
                                                + " -999, -999, -999",
                                        "19510816, 1200, L , TS, 14.0N,  51.0W,  50, -999, -999,"
                                                + " -999, -999, -999, -999, -999, -999, -999, -999,"
                                                + " -999, -999, -999",
                                        "19510817, 0000, L , TS, 14.9N,  54.6W,  60, -999, -999,"
                                                + " -999, -999, -999, -999, -999, -999, -999, -999,"
                                                + " -999, -999, -999"))
                        .startLineNumber(1)
                        .endLineNumber(5)
                        .build();

        ProcessedBatch.ProcessedBatchBuilder expectedBatchBuilder =
                ProcessedBatch.createEmptyBatch(rawBatch.getBatchId());
        ProcessedBatch.PartialCyclone partialCyclone1 =
                ProcessedBatch.PartialCyclone.builder()
                        .cycloneId("AL041851")
                        .header(HeaderLine.parse("AL041851,            UNNAMED,     49,"))
                        .dataLines(
                                List.of(
                                        DataLine.parse(
                                                "19510816, 0000, L , TS, 13.4N,  48.0W,  40, -999,"
                                                    + " -999, -999, -999, -999, -999, -999, -999,"
                                                    + " -999, -999, -999, -999, -999"),
                                        DataLine.parse(
                                                "19510816, 0600, L , TS, 13.7N,  49.5W,  40, -999,"
                                                    + " -999, -999, -999, -999, -999, -999, -999,"
                                                    + " -999, -999, -999, -999, -999"),
                                        DataLine.parse(
                                                "19510816, 1200, L , TS, 14.0N,  51.0W,  50, -999,"
                                                    + " -999, -999, -999, -999, -999, -999, -999,"
                                                    + " -999, -999, -999, -999, -999"),
                                        DataLine.parse(
                                                "19510817, 0000, L , TS, 14.9N,  54.6W,  60, -999,"
                                                    + " -999, -999, -999, -999, -999, -999, -999,"
                                                    + " -999, -999, -999, -999, -999")))
                        .isHeaderPresent(true)
                        .isComplete(false)
                        .build();

        expectedBatchBuilder.partialCyclones(List.of(partialCyclone1));
        ProcessedBatch expectedBatch = expectedBatchBuilder.valid(true).build();

        ProcessedBatch result = batchProcessorService.processBatch(rawBatch).join();

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("processingTimeMs")
                .isEqualTo(expectedBatch);

        // Optionally, you can also assert other fields or use more detailed comparisons as needed.
        log.info("Validation successful: {}", result);
    }

    // Test method for validateBatch
    @Test
    void testValidateBatch_Valid() {
        ProcessedBatch validBatch =
                ProcessedBatch.builder()
                        .batchId(1)
                        .partialCyclones(new ArrayList<>())
                        .valid(true)
                        .build();

        Assertions.assertTrue(batchProcessorService.validateBatch(validBatch));
        log.info("Batch validation successful: {}", validBatch);
    }

    // Test method for validateBatch with errors
    @Test
    void testValidateBatch_WithErrors() {
        ProcessedBatch invalidBatch =
                ProcessedBatch.builder()
                        .batchId(2)
                        .validationErrors(new ArrayList<>())
                        .partialCyclones(new ArrayList<>())
                        .valid(false)
                        .build();

        invalidBatch.getValidationErrors().add("Header but no data lines");

        Assertions.assertFalse(batchProcessorService.validateBatch(invalidBatch));
        log.info("Batch validation failed as expected: {}", invalidBatch);
    }
}
