/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.domain;

/**
 * Immutable value object that controls the landfall detection strategy used by
 * {@code LandfallFilterService#filterByAreaLandfallAdvanced}.
 *
 * <ul>
 *   <li><b>useLMarker</b> — when {@code true} (default) only data lines flagged with the HURDAT2
 *       {@code L} record identifier are considered a landfall event (existing behaviour). When
 *       {@code false} any track point that falls within the target boundary qualifies
 *       (F-REQ-4-a).
 *   <li><b>hurricaneOnly</b> — when {@code true} only cyclones that reached hurricane strength
 *       (wind speed >= {@code minWindSpeedKnots}) at the landfall point are included (F-REQ-4-b).
 *   <li><b>minWindSpeedKnots</b> — minimum wind speed threshold in knots; defaults to 64 kt
 *       (Saffir-Simpson Category 1 lower bound).
 *   <li><b>useFloridaPolygon</b> — when {@code true} the containment check uses Florida's
 *       approximate polygon ({@link FloridaPolygon}) instead of the rectangular bounding box
 *       returned by the geocoding service (F-REQ-4-c). Ignored for non-Florida areas.
 * </ul>
 */
public record HurricaneFilterCriteria(
        boolean useLMarker,
        boolean hurricaneOnly,
        int minWindSpeedKnots,
        boolean useFloridaPolygon) {

    /** Default: L-marker detection, no hurricane filter, no polygon check. */
    public static HurricaneFilterCriteria defaults() {
        return new HurricaneFilterCriteria(true, false, 64, false);
    }

    /** F-REQ-4-a: detect landfall by geo-coordinate without requiring the L marker. */
    public static HurricaneFilterCriteria withNoLMarker() {
        return new HurricaneFilterCriteria(false, false, 64, false);
    }

    /** F-REQ-4-b: only include cyclones that reached hurricane strength (>= 64 kt) at landfall. */
    public static HurricaneFilterCriteria withHurricaneOnly() {
        return new HurricaneFilterCriteria(true, true, 64, false);
    }

    /** F-REQ-4-c: use Florida's polygon instead of bounding box for landfall verification. */
    public static HurricaneFilterCriteria withPolygon() {
        return new HurricaneFilterCriteria(true, false, 64, true);
    }

    /**
     * Builder-style factory for all options combined.
     *
     * @param useLMarker        require HURDAT2 L record identifier
     * @param hurricaneOnly     require hurricane-strength wind speed at landfall
     * @param minWindSpeedKnots wind speed threshold (knots) when {@code hurricaneOnly} is true
     * @param useFloridaPolygon use Florida polygon rather than bounding box
     */
    public static HurricaneFilterCriteria of(
            boolean useLMarker,
            boolean hurricaneOnly,
            int minWindSpeedKnots,
            boolean useFloridaPolygon) {
        return new HurricaneFilterCriteria(useLMarker, hurricaneOnly, minWindSpeedKnots, useFloridaPolygon);
    }
}
