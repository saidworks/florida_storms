/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.io;

import com.saidworks.florida_storms.models.Cyclone;
import com.saidworks.florida_storms.models.DataLine;
import com.saidworks.florida_storms.models.HeaderLine;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
@Log4j2
public class CycloneDataParser {
    private Resource sourceFile;

    public CycloneDataParser(Resource sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Reads cyclone tracking data from a text file
     * This method can handle multiple cyclones in a single file
     */
    public List<Cyclone> fromFile() throws IOException {
        List<Cyclone> result = new ArrayList<>();
        log.info("Reading cyclone tracking data from file: {}", sourceFile.getFilename());
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile.getFile()))) {
            String line;
            Cyclone currentCyclone = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Check if this is a header line (starts with AL)
                if (line.startsWith("AL")) {
                    // Create a new cyclone with this header
                    HeaderLine header = HeaderLine.parse(line);
                    currentCyclone = new Cyclone(header);
                    result.add(currentCyclone);
                } else if (currentCyclone != null) {
                    // if cyclone already instantiated add data line to current cyclone
                    currentCyclone.getDataLines().add(DataLine.parse(line));
                }
            }
        }

        return result;
    }
}
