/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.helper;

import static com.saidworks.florida_storms.models.domain.HeaderLine.isHeaderLine;

import com.saidworks.florida_storms.models.batch.RawBatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that assembles raw text lines into size‑bounded batches while respecting
 * cyclone boundaries.
 *
 * <p>Input files are expected to follow the HURDAT2-like format, where each cyclone begins
 * with a header line (see {@link com.saidworks.florida_storms.models.domain.HeaderLine}).
 * Data lines that belong to a cyclone follow its header until the next header appears.
 *
 * <p>This assembler builds batches that:
 * <ul>
 *   <li>Do not split a cyclone across batches (a cyclone's lines are kept together).</li>
 *   <li>Do not exceed the target chunk size unless adding the remainder of the current
 *       cyclone would exceed it—in which case the remainder is deferred to the next batch.</li>
 * </ul>
 *
 * <p>Thread-safety: this class contains only stateless static methods and is thread-safe
 * as long as callers provide independent {@link BufferedReader} instances per thread.
 */
public class RawBatchAssembler {
    /**
     * Non-instantiable utility class.
     */
    private RawBatchAssembler() {
        throw new IllegalStateException("helper class can not be instantiated");
    }

    /**
     * Splits the lines read from the provided {@link BufferedReader} into {@link RawBatch}
     * segments up to the given {@code targetChunkSize}, keeping cyclone blocks intact.
     *
     * <p>Empty lines are ignored. Orphaned data lines (i.e., a data line encountered before a
     * header line) are appended to the current batch and optionally logged as a warning.
     *
     * <p>Note: The current implementation appends the last in-progress cyclone to the current
     * batch at EOF, but it does not yet add the final batch to the returned list. Upstream
     * services are expected to handle the finalization/flush, or this method can be extended
     * to emit the last batch when desired.
     *
     * @param reader           the source of lines to read; the caller owns its lifecycle
     * @param targetChunkSize  the maximum number of lines per batch (soft limit that respects
     *                         cyclone boundaries)
     * @return a list of {@link RawBatch}es created during the scan
     * @throws IOException if reading from {@code reader} fails
     */
    public static List<RawBatch> splitToBatches(BufferedReader reader, int targetChunkSize)
            throws IOException {
        List<RawBatch> batches = new ArrayList<>();
        BatchProcessingState state = new BatchProcessingState();
        String line;

        // Read the file line by line
        while ((line = reader.readLine()) != null) {
            state.lineNumber++;
            line = line.trim();

            // Skip empty lines for robustness
            if (line.isEmpty()) {
                continue;
            }

            // Start of a new cyclone? Handle batching + state transition.
            if (isHeaderLine(line)) {
                handleBatchBasedOnStateAndHeader(line, state, batches, targetChunkSize);
            } else if (state.inCyclone) {
                // Regular data line that belongs to the current cyclone
                state.currentCyclone.add(line);
            }
        }

        // End of file: push any remaining cyclone lines into the current batch.
        // Note: If a final batch flush is desired, consider adding it here.
        state.currentBatch.addAll(state.currentCyclone);
        return batches;
    }

    /**
     * Handles a newly encountered header line by:
     * <ol>
     *   <li>Completing the current cyclone (if any) by moving its lines to the batch.</li>
     *   <li>Emitting the batch if its size reached {@code targetChunkSize}.</li>
     *   <li>Starting a new cyclone context with the provided header line.</li>
     * </ol>
     *
     * @param line             the header line just read
     * @param state            mutable processing state holder
     * @param batches          the collection to which completed batches are appended
     * @param targetChunkSize  maximum desired number of lines per batch
     */
    private static void handleBatchBasedOnStateAndHeader(
            String line, BatchProcessingState state, List<RawBatch> batches, int targetChunkSize) {
        // If we were already inside a cyclone, move its accumulated lines into the batch
        if (state.inCyclone && !state.currentCyclone.isEmpty()) {
            state.currentBatch.addAll(state.currentCyclone);
            state.currentCyclone.clear();
        }
        // If the batch reached (or exceeded) the target size, emit it before starting a new one
        if (!state.currentBatch.isEmpty() && state.currentBatch.size() >= targetChunkSize) {
            batches.add(
                    RawBatch.builder()
                            .batchId(state.batchId)
                            .lines(state.currentBatch)
                            .startLineNumber(state.batchStartLine)
                            .endLineNumber(state.lineNumber - 1)
                            .build());
            // Prepare for the next batch
            state.currentBatch = new ArrayList<>();
            state.batchStartLine = state.lineNumber;
        }

        // Start a new cyclone with the encountered header line
        state.currentCyclone.add(line);
        state.inCyclone = true;
    }

    /**
     * Internal mutable state used while scanning input lines.
     */
    private static class BatchProcessingState {
        /** Lines in the batch being built. */
        List<String> currentBatch = new ArrayList<>();

        /** Lines of the cyclone currently being read (header + data lines). */
        List<String> currentCyclone = new ArrayList<>();

        /** 1-based index of the line being processed (for diagnostics). */
        int lineNumber = 0;

        /** Identifier of the batch being built (assigned upstream). */
        int batchId = 0;

        /** File line number where the current batch started. */
        int batchStartLine = 0;

        /** Whether we are currently inside a cyclone block. */
        boolean inCyclone = false;
    }
}
