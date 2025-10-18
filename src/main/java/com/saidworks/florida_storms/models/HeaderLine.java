package com.saidworks.florida_storms.models;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class HeaderLine {
    private String basin;        // AL, EP, CP
    private int cycloneNumber;   // 01
    private int year;            // 1851
    private String name;         // UNNAMED
    private int entriesCount;    // 14

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

        if (headerData.length() < 8) {
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
        return String.format("%s,            %s,     %d,",
                getCycloneId(), name, entriesCount);
    }
}