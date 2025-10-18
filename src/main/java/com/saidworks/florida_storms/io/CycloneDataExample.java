/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.io;

import com.saidworks.florida_storms.models.Cyclone;
import com.saidworks.florida_storms.models.DataLine;
import com.saidworks.florida_storms.models.HeaderLine;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CycloneDataExample{
    private CycloneDataParser parser;

    public CycloneDataExample(CycloneDataParser parser) {
        this.parser = parser;
    }

    public void start() {
        try {
            List<Cyclone> cyclones;
            cyclones = parser.fromFile();

            // Print information about all cyclones
            log.info("Found {} cyclones in the file", cyclones.size());

            for (int i = 0; i < cyclones.size(); i++) {
                Cyclone cyclone = cyclones.get(i);
                HeaderLine header = cyclone.getHeader();

                log.info("\n--- Cyclone #{} ---", (i + 1));
                log.info("ID: {}", header.getCycloneId());
                log.info("Basin: {}", header.getBasin());
                log.info("Cyclone Number: {}", header.getCycloneNumber());
                log.info("Year: {}", header.getYear());
                log.info("Name: {}", header.getName());
                log.info("Expected Entries: {}", header.getEntriesCount());
                log.info("Actual Entries: {}", cyclone.getDataLines().size());

                if (!cyclone.isDataCompletePerHeader()) {
                    log.warn("WARNING: Data is incomplete!");
                }

                // Print first few data points
                List<DataLine> dataLines = cyclone.getDataLines();
                int linesToShow = Math.min(3, dataLines.size());

                log.info("\nFirst {} data points:", linesToShow);
                for (int j = 0; j < linesToShow; j++) {
                    DataLine dataLine = dataLines.get(j);
                    log.info(
                            "  {} - Status: {} - Wind: {} knots{}",
                            dataLine.getDateTime(),
                            dataLine.getSystemStatus(),
                            dataLine.getMaxSustainedWind(),
                            (dataLine.isLandfall() ? " (LANDFALL)" : ""));
                }

                // Check for landfall events
                log.info("\nLandfall events:");
                boolean hasLandfall = false;
                for (DataLine dataLine : dataLines) {
                    if (dataLine.isLandfall()) {
                        hasLandfall = true;
                        log.info(
                                "  {} at {}°{}, {}°{} - Wind: {} knots",
                                dataLine.getDateTime(),
                                dataLine.getLatitude(),
                                dataLine.getLatitudeHemisphere(),
                                dataLine.getLongitude(),
                                dataLine.getLongitudeHemisphere(),
                                dataLine.getMaxSustainedWind());
                    }
                }
                if (!hasLandfall) {
                    log.info("  None recorded");
                }
            }

        } catch (Exception e) {
            log.error("Error processing cyclone data: {}", e.getMessage(), e);
        }
    }
}
