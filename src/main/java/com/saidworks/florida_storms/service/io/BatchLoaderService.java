/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.io;

import com.saidworks.florida_storms.config.CycloneProcessingProperties;
import com.saidworks.florida_storms.models.RawBatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Service responsible for loading the file and splitting it into raw batches
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class BatchLoaderService {
    private final CycloneProcessingProperties properties;
    private final ResourceLoader resourceLoader;

    /**
     * Loads the cyclone data file and splits it into raw batches
     * @return List of raw batches ready for processing
     */
    public List<RawBatch> loadBatches() throws IOException {
        Resource resource = resourceLoader.getResource(properties.getData().getHurdat2());
        List<RawBatch> batches = new ArrayList<>();
        int chunkSize = properties.getProcessing().getChunkSize();

        log.info("Loading cyclone data from: {}", resource.getFilename());
        log.info("Batch chunk size: {}", chunkSize);

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            List<String> currentBatch = new ArrayList<>();
            int lineNumber = 0;
            int batchId = 0;
            int batchStartLine = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                currentBatch.add(line);

                // Create batch when chunk size is reached
                if (currentBatch.size() >= chunkSize) {
                    batches.add(createBatch(batchId++, currentBatch, batchStartLine, lineNumber));
                    currentBatch = new ArrayList<>();
                    batchStartLine = lineNumber;
                }
            }

            // Add remaining lines as final batch
            if (!currentBatch.isEmpty()) {
                batches.add(createBatch(batchId, currentBatch, batchStartLine, lineNumber));
            }
        }

        log.info(
                "Loaded {} batches from file (total lines: {})",
                batches.size(),
                batches.stream().mapToInt(b -> b.getLines().size()).sum());
        return batches;
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
