/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataLine {
    private LocalDateTime dateTime;
    private Character recordType; // L, P, I, S, T or null for space/empty
    private String stormStatus; // TD, TS, HU, EX, SD, SS, LO, DB

    private double latitude;
    private char latitudeDirection; // N, S
    private double longitude;
    private char longitudeDirection; // W, E

    private int maxWindSpeed; // knots
    private Integer centralPressure; // millibars, can be -999 for missing data

    // 34kt wind radii (nautical miles), can be -999 for missing data
    private Integer windRadius34NE;
    private Integer windRadius34SE;
    private Integer windRadius34SW;
    private Integer windRadius34NW;

    // 50kt wind radii (nautical miles), can be -999 for missing data
    private Integer windRadius50NE;
    private Integer windRadius50SE;
    private Integer windRadius50SW;
    private Integer windRadius50NW;

    // 64kt wind radii (nautical miles), can be -999 for missing data
    private Integer windRadius64NE;
    private Integer windRadius64SE;
    private Integer windRadius64SW;
    private Integer windRadius64NW;

    // Radius of maximum wind (nautical miles), can be -999 for missing data
    private Integer maxWindRadius;

    /**
     * Parses a data line in the format:
     * 18510625, 0000,  , HU, 28.0N,  94.8W,  80, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
     */
    public static DataLine parse(String line) {
        String[] parts = line.split(",");
        if (parts.length < 20) { // Minimum 20 fields are expected
            throw new IllegalArgumentException(
                    "Invalid data line format: not enough fields in line: " + line);
        }

        // Parse date and time
        String dateStr = parts[0].trim();
        String timeStr = parts[1].trim();

        int year = Integer.parseInt(dateStr.substring(0, 4));
        int month = Integer.parseInt(dateStr.substring(4, 6));
        int day = Integer.parseInt(dateStr.substring(6));
        int hour = Integer.parseInt(timeStr.substring(0, 2));
        int minute = Integer.parseInt(timeStr.substring(2));

        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute);

        // Parse record identifier (can be empty)
        String recordTypeStr = parts[2].trim();
        Character recordType = recordTypeStr.isEmpty() ? null : recordTypeStr.charAt(0);

        // Parse latitude and longitude
        String latStr = parts[4].trim();
        String lonStr = parts[5].trim();

        // Use separate patterns for latitude and longitude for hemisphere validation
        Pattern latPattern = Pattern.compile("(\\d+\\.\\d+)([NS])");
        Pattern lonPattern = Pattern.compile("(\\d+\\.\\d+)([WE])");

        // Parse latitude
        Matcher latMatcher = latPattern.matcher(latStr);
        if (!latMatcher.matches()) {
            throw new IllegalArgumentException("Invalid latitude format: " + latStr);
        }
        double latitude = Double.parseDouble(latMatcher.group(1));
        char latitudeDirection = latMatcher.group(2).charAt(0);

        // Parse longitude
        Matcher lonMatcher = lonPattern.matcher(lonStr);
        if (!lonMatcher.matches()) {
            throw new IllegalArgumentException("Invalid longitude format: " + lonStr);
        }
        double longitude = Double.parseDouble(lonMatcher.group(1));
        char longitudeDirection = lonMatcher.group(2).charAt(0);

        // Parse integer values (handling -999 as missing data)
        int maxWindSpeed = Integer.parseInt(parts[6].trim());

        Integer centralPressure = parseIntWithMissing(parts[7].trim());
        Integer windRadius34NE = parseIntWithMissing(parts[8].trim());
        Integer windRadius34SE = parseIntWithMissing(parts[9].trim());
        Integer windRadius34SW = parseIntWithMissing(parts[10].trim());
        Integer windRadius34NW = parseIntWithMissing(parts[11].trim());
        Integer windRadius50NE = parseIntWithMissing(parts[12].trim());
        Integer windRadius50SE = parseIntWithMissing(parts[13].trim());
        Integer windRadius50SW = parseIntWithMissing(parts[14].trim());
        Integer windRadius50NW = parseIntWithMissing(parts[15].trim());
        Integer windRadius64NE = parseIntWithMissing(parts[16].trim());
        Integer windRadius64SE = parseIntWithMissing(parts[17].trim());
        Integer windRadius64SW = parseIntWithMissing(parts[18].trim());
        Integer windRadius64NW = parseIntWithMissing(parts[19].trim());

        // maxWindRadius is optional as it's not in all HURDAT2 versions
        Integer maxWindRadius = parts.length > 20 ? parseIntWithMissing(parts[20].trim()) : null;

        // Build the data line object
        return DataLine.builder()
                .dateTime(dateTime)
                .recordType(recordType)
                .stormStatus(parts[3].trim())
                .latitude(latitude)
                .latitudeDirection(latitudeDirection)
                .longitude(longitude)
                .longitudeDirection(longitudeDirection)
                .maxWindSpeed(maxWindSpeed)
                .centralPressure(centralPressure)
                .windRadius34NE(windRadius34NE)
                .windRadius34SE(windRadius34SE)
                .windRadius34SW(windRadius34SW)
                .windRadius34NW(windRadius34NW)
                .windRadius50NE(windRadius50NE)
                .windRadius50SE(windRadius50SE)
                .windRadius50SW(windRadius50SW)
                .windRadius50NW(windRadius50NW)
                .windRadius64NE(windRadius64NE)
                .windRadius64SE(windRadius64SE)
                .windRadius64SW(windRadius64SW)
                .windRadius64NW(windRadius64NW)
                .maxWindRadius(maxWindRadius)
                .build();
    }

    /**
     * Helper method to parse integer values with -999 representing missing data
     * Returns null for missing data
     */
    private static Integer parseIntWithMissing(String value) {
        int parsedValue = Integer.parseInt(value);
        return (parsedValue == -999) ? null : parsedValue;
    }

    /**
     * Checks if the data line has a landfall record
     */
    public boolean isLandfall() {
        return recordType != null && recordType == 'L';
    }

    /**
     *   check if data line is within range of years
     */
    public boolean isAfter1900() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate cutoffDate = LocalDate.parse("19000101", formatter);
        return dateTime.toLocalDate().isAfter(cutoffDate);
    }
}
