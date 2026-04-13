/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HeaderLine {
    private static final int HEADER_DATA_MIN_LENGTH = 8;

    private String basin; // AL, EP, CP
    private int cycloneNumber; // 01
    private int year; // 1851
    private String name; // UNNAMED
    private int entriesCount; // 14

    /**
     * Quick heuristic to determine whether a text line is a HURDAT2 header line
     * rather than a data line. Headers have just a few commas (typically <= 3),
     * while data lines contain many comma-separated fields (20+).
     *
     * @param line raw line from the dataset
     * @return true if the line looks like a header, false otherwise
     */
    public static boolean isHeaderLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        long commaCount = line.chars().filter(ch -> ch == ',').count();
        return commaCount <= 3;
    }

    /**
     * Parses a header line in the format:
     * AL011851,            UNNAMED,     14,
     */
    public static HeaderLine parse(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid header line format: " + line);
        }

        String headerData = parts[0].trim();
        String name = parts[1].trim();
        String entriesCount = parts[2].trim();

        if (headerData.length() < HEADER_DATA_MIN_LENGTH) {
            throw new IllegalArgumentException("Invalid header data format: " + headerData);
        }

        return HeaderLine.builder()
                .basin(headerData.substring(0, 2))
                .cycloneNumber(Integer.parseInt(headerData.substring(2, 4)))
                .year(Integer.parseInt(headerData.substring(4, 8)))
                .name(name)
                .entriesCount(Integer.parseInt(entriesCount))
                .build();
    }

    /**
     * Generates the cyclone identifier (e.g., "AL011851")
     */
    public String getCycloneId() {
        return String.format("%s%02d%d", basin, cycloneNumber, year);
    }

    @Override
    public String toString() {
        return String.format("%s,            %s,     %d,", getCycloneId(), name, entriesCount);
    }
}
