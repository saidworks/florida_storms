/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.helper;

import static com.saidworks.florida_storms.models.domain.HeaderLine.isHeaderLine;

import com.saidworks.florida_storms.models.batch.RawBatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;

/**
 * Helper responsible for batching logic: grouping lines into RawBatch instances
 * while preserving cyclone boundaries.
 */
public class RawBatchAssembler {
    private RawBatchAssembler() {
        throw new IllegalStateException("helper class can not be instantiated");
    }

    public static List<RawBatch> splitToBatches(
            BufferedReader reader, int targetChunkSize, Logger log) throws IOException {
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
                handleBatchBasedOnStateAndHeader(line, state, batches, targetChunkSize);
            } else if (state.inCyclone) {
                state.currentCyclone.add(line);
            } else {
                handleOrphanedLine(line, state, log);
            }
        }

        state.currentBatch.addAll(state.currentCyclone);
        return batches;
    }

    private static void handleBatchBasedOnStateAndHeader(
            String line, BatchProcessingState state, List<RawBatch> batches, int targetChunkSize) {
        if (state.inCyclone && !state.currentCyclone.isEmpty()) {
            state.currentBatch.addAll(state.currentCyclone);
            state.currentCyclone.clear();
        }
        // add batch if the target chunk size is reached
        if (!state.currentBatch.isEmpty() && state.currentBatch.size() >= targetChunkSize) {
            batches.add(
                    RawBatch.builder()
                            .batchId(state.batchId)
                            .lines(state.currentBatch)
                            .startLineNumber(state.batchStartLine)
                            .endLineNumber(state.lineNumber - 1)
                            .build());
            // clear current batch
            state.currentBatch = new ArrayList<>();
            state.batchStartLine = state.lineNumber;
        }

        state.currentCyclone.add(line);
        state.inCyclone = true;
    }

    private static void handleOrphanedLine(String line, BatchProcessingState state, Logger log) {
        if (log != null) {
            log.warn("Found orphaned data line at line {}: {}", state.lineNumber, line);
        }
        state.currentBatch.add(line);
    }

    private static class BatchProcessingState {
        List<String> currentBatch = new ArrayList<>();
        List<String> currentCyclone = new ArrayList<>();
        int lineNumber = 0;
        int batchId = 0;
        int batchStartLine = 0;
        boolean inCyclone = false;
    }
}
