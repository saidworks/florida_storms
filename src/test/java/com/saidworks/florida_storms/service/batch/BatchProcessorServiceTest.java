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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BatchProcessorServiceTest {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private final BatchProcessorService batchProcessorService =
            new BatchProcessorService(executorService);

    @AfterAll
    static void tearDown() {
        executorService.shutdown();
    }

    @Test
    @DisplayName("processBatch parses header and data lines into a valid partial cyclone")
    void processBatch_validData_parsesCorrectly() {
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
    }

    @Test
    @DisplayName(
            "processBatch stores all post-1900 track points regardless of L marker (F-REQ-4-a)")
    void processBatch_nonLMarkerDataLines_storedForDownstreamFiltering() {
        RawBatch rawBatch =
                RawBatch.builder()
                        .batchId(99)
                        .lines(
                                List.of(
                                        "AL041851,            UNNAMED,     49,",
                                        "19510816, 0000, L , HU, 27.0N,  81.0W,  80, 950, -999,"
                                                + " -999, -999, -999, -999, -999, -999, -999, -999,"
                                                + " -999, -999, -999",
                                        "19510816, 0600,  , HU, 28.0N,  80.5W,  75, 960, -999,"
                                                + " -999, -999, -999, -999, -999, -999, -999, -999,"
                                                + " -999, -999, -999"))
                        .startLineNumber(1)
                        .endLineNumber(3)
                        .build();

        ProcessedBatch result = batchProcessorService.processBatch(rawBatch).join();

        assertThat(result.getPartialCyclones()).hasSize(1);
        assertThat(result.getPartialCyclones().get(0).getDataLines())
                .as("All post-1900 track points should be stored regardless of L marker")
                .hasSize(2);

        assertThat(result.getPartialCyclones().get(0).getDataLines().get(0).isLandfall())
                .as("First data line should be L-marked")
                .isTrue();
        assertThat(result.getPartialCyclones().get(0).getDataLines().get(1).isLandfall())
                .as("Second data line should not be L-marked")
                .isFalse();
    }

    @Test
    @DisplayName("validateBatch returns true for a valid batch")
    void validateBatch_validBatch_returnsTrue() {
        ProcessedBatch validBatch =
                ProcessedBatch.builder()
                        .batchId(1)
                        .partialCyclones(new ArrayList<>())
                        .valid(true)
                        .build();

        assertThat(batchProcessorService.validateBatch(validBatch)).isTrue();
    }

    @Test
    @DisplayName("validateBatch returns false when batch has validation errors")
    void validateBatch_invalidBatch_returnsFalse() {
        ProcessedBatch invalidBatch =
                ProcessedBatch.builder()
                        .batchId(2)
                        .validationErrors(new ArrayList<>())
                        .partialCyclones(new ArrayList<>())
                        .valid(false)
                        .build();

        invalidBatch.getValidationErrors().add("Header but no data lines");

        assertThat(batchProcessorService.validateBatch(invalidBatch)).isFalse();
    }
}
