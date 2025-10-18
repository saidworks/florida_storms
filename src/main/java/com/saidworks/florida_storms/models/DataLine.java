/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataLine {
    private LocalDateTime dateTime;
    private Character recordIdentifier; // L, P, I, S, T or null for space/empty
    private String systemStatus; // TD, TS, HU, EX, SD, SS, LO, DB

    private double latitude;
    private char latitudeHemisphere; // N, S
    private double longitude;
    private char longitudeHemisphere; // W, E

    private int maxSustainedWind; // knots
    private Integer minPressure; // millibars, can be -999 for missing data

    // 34kt wind radii (nautical miles), can be -999 for missing data
    private Integer ne34;
    private Integer se34;
    private Integer sw34;
    private Integer nw34;

    // 50kt wind radii (nautical miles), can be -999 for missing data
    private Integer ne50;
    private Integer se50;
    private Integer sw50;
    private Integer nw50;

    // 64kt wind radii (nautical miles), can be -999 for missing data
    private Integer ne64;
    private Integer se64;
    private Integer sw64;
    private Integer nw64;

    // Radius of maximum wind (nautical miles), can be -999 for missing data
    private Integer radiusMaxWind;

    /**
     * Parses a data line in the format:
     * 18510625, 0000,  , HU, 28.0N,  94.8W,  80, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
     */
    public static DataLine parse(String line) {
        String[] parts = line.split(",");
        if (parts.length < 21) {
            throw new IllegalArgumentException("Invalid data line format: " + line);
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
        String recordIdStr = parts[2].trim();
        Character recordIdentifier = recordIdStr.isEmpty() ? null : recordIdStr.charAt(0);

        // Parse latitude and longitude
        String latStr = parts[4].trim();
        String lonStr = parts[5].trim();

        Pattern coordPattern = Pattern.compile("(\\d+\\.\\d+)([NSWE])");

        // Parse latitude
        Matcher latMatcher = coordPattern.matcher(latStr);
        if (!latMatcher.matches()) {
            throw new IllegalArgumentException("Invalid latitude format: " + latStr);
        }
        double latitude = Double.parseDouble(latMatcher.group(1));
        char latHemisphere = latMatcher.group(2).charAt(0);

        // Parse longitude
        Matcher lonMatcher = coordPattern.matcher(lonStr);
        if (!lonMatcher.matches()) {
            throw new IllegalArgumentException("Invalid longitude format: " + lonStr);
        }
        double longitude = Double.parseDouble(lonMatcher.group(1));
        char lonHemisphere = lonMatcher.group(2).charAt(0);

        // Parse integer values (handling -999 as missing data)
        int maxSustainedWind = Integer.parseInt(parts[6].trim());

        Integer minPressure = parseIntWithMissing(parts[7].trim());
        Integer ne34 = parseIntWithMissing(parts[8].trim());
        Integer se34 = parseIntWithMissing(parts[9].trim());
        Integer sw34 = parseIntWithMissing(parts[10].trim());
        Integer nw34 = parseIntWithMissing(parts[11].trim());
        Integer ne50 = parseIntWithMissing(parts[12].trim());
        Integer se50 = parseIntWithMissing(parts[13].trim());
        Integer sw50 = parseIntWithMissing(parts[14].trim());
        Integer nw50 = parseIntWithMissing(parts[15].trim());
        Integer ne64 = parseIntWithMissing(parts[16].trim());
        Integer se64 = parseIntWithMissing(parts[17].trim());
        Integer sw64 = parseIntWithMissing(parts[18].trim());
        Integer nw64 = parseIntWithMissing(parts[19].trim());
        Integer radiusMaxWind = parseIntWithMissing(parts[20].trim());

        // Build the data line object
        return DataLine.builder()
                .dateTime(dateTime)
                .recordIdentifier(recordIdentifier)
                .systemStatus(parts[3].trim())
                .latitude(latitude)
                .latitudeHemisphere(latHemisphere)
                .longitude(longitude)
                .longitudeHemisphere(lonHemisphere)
                .maxSustainedWind(maxSustainedWind)
                .minPressure(minPressure)
                .ne34(ne34)
                .se34(se34)
                .sw34(sw34)
                .nw34(nw34)
                .ne50(ne50)
                .se50(se50)
                .sw50(sw50)
                .nw50(nw50)
                .ne64(ne64)
                .se64(se64)
                .sw64(sw64)
                .nw64(nw64)
                .radiusMaxWind(radiusMaxWind)
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
        return recordIdentifier != null && recordIdentifier == 'L';
    }

    @Override
    public String toString() {
        String recordIdStr = (recordIdentifier == null) ? " " : recordIdentifier.toString();

        return String.format(
                "%04d%02d%02d, %04d, %s, %s, %4.1f%c, %5.1f%c, %3d, %4s, %4s, %4s, %4s, %4s, %4s,"
                        + " %4s, %4s, %4s, %4s, %4s, %4s, %4s, %4s",
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.getHour() * 100 + dateTime.getMinute(),
                recordIdStr,
                systemStatus,
                latitude,
                latitudeHemisphere,
                longitude,
                longitudeHemisphere,
                maxSustainedWind,
                formatMissingValue(minPressure),
                formatMissingValue(ne34),
                formatMissingValue(se34),
                formatMissingValue(sw34),
                formatMissingValue(nw34),
                formatMissingValue(ne50),
                formatMissingValue(se50),
                formatMissingValue(sw50),
                formatMissingValue(nw50),
                formatMissingValue(ne64),
                formatMissingValue(se64),
                formatMissingValue(sw64),
                formatMissingValue(nw64),
                formatMissingValue(radiusMaxWind));
    }

    /**
     * Helper method to format values for output, displaying -999 for missing data
     */
    private String formatMissingValue(Integer value) {
        return (value == null) ? "-999" : value.toString();
    }
}
