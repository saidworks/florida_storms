/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.domain;

/**
 * Simplified polygon boundary for the state of Florida used for accurate landfall detection.
 *
 * <p>Addresses F-REQ-4-c: rather than relying on a rectangular bounding box (which includes large
 * sections of the Gulf of Mexico and Atlantic Ocean), this polygon traces Florida's coastline so
 * that only coordinates that physically fall on land are counted as Florida landfalls.
 *
 * <p>The polygon is an approximation (~25 key boundary points going clockwise from the northeast
 * corner) with enough resolution to match the spatial granularity of HURDAT2 track data
 * (~50–100 km between records). Interior accuracy matters more than coastal precision here.
 */
public final class FloridaPolygon {

    /**
     * Approximate Florida boundary. Each entry is {latitude, longitude} in signed decimal degrees
     * (negative longitude = west). Points go clockwise starting at the northeast corner
     * (Georgia/Atlantic border).
     */
    private static final double[][] POLYGON = {
        // Northeast corner — Georgia/Atlantic border
        {31.00, -82.05},
        // Southeast Atlantic coast (going south)
        {30.71, -81.44}, // Jacksonville
        {30.10, -81.37}, // St. Augustine
        {29.49, -81.07}, // Flagler Beach
        {28.93, -80.69}, // Cape Canaveral
        {28.07, -80.61}, // Melbourne / Vero Beach
        {27.45, -80.35}, // Fort Pierce
        {26.69, -80.04}, // West Palm Beach
        {25.78, -80.13}, // Fort Lauderdale
        {25.48, -80.42}, // Miami
        {25.13, -80.90}, // Florida City (mainland tip)
        // Southern tip — cross the bottom of the peninsula
        {24.55, -81.40}, // Cape Sable area
        // West coast (going north)
        {25.18, -81.12}, // Everglades / Big Cypress
        {25.75, -81.41}, // Naples
        {26.43, -82.00}, // Fort Myers
        {26.92, -82.49}, // Charlotte / Sarasota area
        {27.22, -82.95}, // Venice / Englewood
        {27.76, -82.77}, // Tampa Bay south
        {28.06, -82.59}, // Tampa Bay north
        {28.52, -82.89}, // Spring Hill
        {28.97, -83.01}, // Crystal River
        {29.46, -83.26}, // Horseshoe Beach
        {30.02, -85.65}, // Panama City area
        {30.35, -86.57}, // Fort Walton Beach
        {30.57, -87.16}, // Pensacola
        // Northwest corner — Alabama border
        {31.00, -87.61},
        // North border — east along Georgia/Alabama state line
        {31.00, -85.00},
        // Back to start
        {31.00, -82.05},
    };

    private FloridaPolygon() {
        // utility class — no instances
    }

    /**
     * Returns {@code true} if the given coordinate falls inside Florida's polygon.
     *
     * <p>Uses the ray-casting algorithm: a horizontal ray is cast east from the test point and the
     * number of polygon-edge crossings is counted. An odd count means the point is inside.
     *
     * @param lat signed decimal latitude (positive = north)
     * @param lon signed decimal longitude (negative = west)
     */
    public static boolean containsPoint(double lat, double lon) {
        boolean inside = false;
        int n = POLYGON.length;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double latI = POLYGON[i][0];
            double lonI = POLYGON[i][1];
            double latJ = POLYGON[j][0];
            double lonJ = POLYGON[j][1];

            // Check if edge [j → i] crosses the horizontal ray cast east from (lat, lon)
            boolean crossesLonBand = (lonI > lon) != (lonJ > lon);
            if (crossesLonBand) {
                double intersectLat = latI + (latJ - latI) * (lon - lonI) / (lonJ - lonI);
                if (lat < intersectLat) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }
}
