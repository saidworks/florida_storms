/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a batch after processing with parsed data and validation results
 */
@Data
@Builder
public class ProcessedBatch {
    private int batchId;
    private List<PartialCyclone> partialCyclones;
    private List<String> validationErrors;
    private boolean valid;
    private long processingTimeMs;

    /**
     * Represents a partial cyclone that may span multiple batches
     */
    @Data
    @Builder
    public static class PartialCyclone {
        private String cycloneId; // e.g., "AL011851"
        private HeaderLine header; // May be null if batch starts mid-cyclone
        private List<DataLine> dataLines;
        private boolean isHeaderPresent;
        private boolean isComplete; // True if we know this is the last batch for this cyclone
    }

    public static ProcessedBatch.ProcessedBatchBuilder createEmpty(int batchId) {
        return ProcessedBatch.builder()
                .batchId(batchId)
                .partialCyclones(new ArrayList<>())
                .validationErrors(new ArrayList<>())
                .valid(true)
                .processingTimeMs(0L);
    }
}
