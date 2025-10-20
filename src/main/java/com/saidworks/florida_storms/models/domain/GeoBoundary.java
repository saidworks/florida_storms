/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents geographic boundaries for a state or area
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoBoundary {
    private String name;
    private double minLatitude;
    private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;

    /**
     * Checks if a given coordinate is within this boundary
     */
    public boolean containsCoordinate(
            double latitude, char latDirection, double longitude, char lonDirection) {
        // Convert to signed coordinates
        double signedLat = latDirection == 'S' ? -latitude : latitude;
        double signedLon = lonDirection == 'W' ? -longitude : longitude;

        return signedLat >= minLatitude
                && signedLat <= maxLatitude
                && signedLon >= minLongitude
                && signedLon <= maxLongitude;
    }
}
