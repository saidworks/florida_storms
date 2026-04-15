/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.landfall;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.domain.DataLine;
import com.saidworks.florida_storms.models.domain.FloridaPolygon;
import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.models.domain.HurricaneFilterCriteria;
import com.saidworks.florida_storms.models.exception.GeocodingException;
import com.saidworks.florida_storms.service.port.CycloneDataPort;
import com.saidworks.florida_storms.service.port.GeocodingPort;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service to filter storms that made landfall in specific geographic areas
 */
@Service
@Log4j2
public class LandfallFilterService {

    private final CycloneDataPort cycloneDataPort;
    private final GeocodingPort geocodingPort;
    private final ExecutorService serviceTaskExecutor;
    private final ExecutorService ioBlockingTaskExecutor;

    public LandfallFilterService(
            CycloneDataPort cycloneDataPort,
            GeocodingPort geocodingPort,
            @Qualifier("serviceTaskExecutor") ExecutorService serviceTaskExecutor,
            @Qualifier("ioBlockingTaskExecutor") ExecutorService ioBlockingTaskExecutor) {
        this.cycloneDataPort = cycloneDataPort;
        this.geocodingPort = geocodingPort;
        this.serviceTaskExecutor = serviceTaskExecutor;
        this.ioBlockingTaskExecutor = ioBlockingTaskExecutor;
    }

    /**
     * Filters storms that made landfall in a specific geographic area
     * @param areaName The area name (e.g., "Miami", "Gulf Coast")
     * @return CompletableFuture with list of filtered cyclones
     */
    public CompletableFuture<List<Cyclone>> filterByAreaLandfall(String areaName) {
        log.info("Starting landfall filter for area: {}", areaName);

        CompletableFuture<GeoBoundary> boundaryFuture = geocodingPort.getAreaBoundaries(areaName);

        CompletableFuture<List<Cyclone>> cyclonesFuture =
                CompletableFuture.supplyAsync(() -> loadCyclones(), ioBlockingTaskExecutor);

        return boundaryFuture.thenCombineAsync(
                cyclonesFuture,
                (boundary, cyclones) -> {
                    log.info("Filtering {} cyclones for area boundaries", cyclones.size());
                    return filterCyclonesByBoundary(cyclones, boundary);
                },
                serviceTaskExecutor);
    }

    /**
     * Filters cyclones by custom latitude/longitude boundaries (L-marker detection).
     */
    public CompletableFuture<List<Cyclone>> filterByCustomBoundaries(
            double minLat, double maxLat, double minLon, double maxLon) {

        GeoBoundary customBoundary =
                GeoBoundary.builder()
                        .name("Custom Area")
                        .minLatitude(minLat)
                        .maxLatitude(maxLat)
                        .minLongitude(minLon)
                        .maxLongitude(maxLon)
                        .build();

        return CompletableFuture.supplyAsync(
                () -> {
                    List<Cyclone> cyclones = loadCyclones();
                    return filterCyclonesByBoundary(cyclones, customBoundary);
                },
                serviceTaskExecutor);
    }

    /**
     * Advanced landfall filter supporting F-REQ-4-a/b/c detection strategies.
     *
     * <ul>
     *   <li>F-REQ-4-a: pass {@code criteria.useLMarker() == false} to detect landfall by
     *       geo-coordinate without requiring the HURDAT2 L record identifier.
     *   <li>F-REQ-4-b: pass {@code criteria.hurricaneOnly() == true} to include only cyclones
     *       that reached hurricane strength (>= 64 kt) at the landfall point.
     *   <li>F-REQ-4-c: pass {@code criteria.useFloridaPolygon() == true} to verify coordinates
     *       against Florida's polygon instead of its rectangular bounding box.
     * </ul>
     *
     * @param areaName name of the geographic area (resolved via geocoding service)
     * @param criteria detection strategy configuration
     */
    public CompletableFuture<List<Cyclone>> filterByAreaLandfallAdvanced(
            String areaName, HurricaneFilterCriteria criteria) {
        log.info(
                "Starting advanced landfall filter for area: {} with criteria: {}",
                areaName,
                criteria);

        CompletableFuture<GeoBoundary> boundaryFuture = geocodingPort.getAreaBoundaries(areaName);

        CompletableFuture<List<Cyclone>> cyclonesFuture =
                CompletableFuture.supplyAsync(() -> loadCyclones(), ioBlockingTaskExecutor);

        return boundaryFuture.thenCombineAsync(
                cyclonesFuture,
                (boundary, cyclones) -> {
                    log.info(
                            "Applying advanced filter on {} cyclones for area: {}",
                            cyclones.size(),
                            boundary.getName());
                    return filterCyclonesByBoundaryAdvanced(cyclones, boundary, areaName, criteria);
                },
                serviceTaskExecutor);
    }

    /**
     * Core filtering logic — L-marker + bounding-box (original behaviour, preserved for
     * backward-compatibility with existing endpoints and Cucumber scenarios).
     *
     * <p>Because {@code BatchProcessorService} now stores all post-1900 track points, this method
     * explicitly re-applies the L-marker check so the existing count of 167 Florida landfalls
     * is preserved.
     */
    private List<Cyclone> filterCyclonesByBoundary(List<Cyclone> cyclones, GeoBoundary boundary) {
        log.info("Applying boundary filter: {}", boundary.getName());

        List<Cyclone> filteredCyclones =
                cyclones.stream()
                        .filter(cyclone -> hasLandfallInBoundary(cyclone, boundary))
                        .toList();

        log.info(
                "Filtered {} cyclones with landfall in {}",
                filteredCyclones.size(),
                boundary.getName());
        return filteredCyclones;
    }

    /**
     * Advanced core filtering — dispatches to the appropriate detection strategy based on
     * {@code criteria}.
     */
    private List<Cyclone> filterCyclonesByBoundaryAdvanced(
            List<Cyclone> cyclones,
            GeoBoundary boundary,
            String areaName,
            HurricaneFilterCriteria criteria) {

        List<Cyclone> filteredCyclones =
                cyclones.stream()
                        .filter(
                                cyclone -> {
                                    if (criteria.useFloridaPolygon()
                                            && areaName.equalsIgnoreCase("Florida")) {
                                        return hasLandfallInPolygon(cyclone, criteria);
                                    } else if (!criteria.useLMarker()) {
                                        return hasCoordinateInBoundary(cyclone, boundary, criteria);
                                    } else if (criteria.hurricaneOnly()) {
                                        return hasHurricaneLandfallInBoundary(
                                                cyclone, boundary, criteria.minWindSpeedKnots());
                                    } else {
                                        return hasLandfallInBoundary(cyclone, boundary);
                                    }
                                })
                        .toList();

        log.info(
                "Advanced filter returned {} cyclones in {} (criteria: {})",
                filteredCyclones.size(),
                boundary.getName(),
                criteria);
        return filteredCyclones;
    }

    /**
     * Loads all cyclones from the data port, converting checked IOException to unchecked.
     */
    private List<Cyclone> loadCyclones() {
        try {
            return cycloneDataPort.processAllCyclones();
        } catch (IOException e) {
            log.error("Error loading cyclones from data source", e);
            throw new GeocodingException("Failed to load cyclones", e);
        }
    }

    // -------------------------------------------------------------------------
    // Detection-strategy predicates
    // -------------------------------------------------------------------------

    /**
     * F-REQ-4-a (default/original): cyclone has at least one L-marked track point inside the
     * bounding box.
     */
    private boolean hasLandfallInBoundary(Cyclone cyclone, GeoBoundary boundary) {
        return cyclone.getDataLines().stream()
                .filter(DataLine::isLandfall)
                .anyMatch(dataLine -> inBoundingBox(dataLine, boundary));
    }

    /**
     * F-REQ-4-a (alternative): any track point inside the bounding box counts as landfall —
     * no L marker required.
     */
    private boolean hasCoordinateInBoundary(
            Cyclone cyclone, GeoBoundary boundary, HurricaneFilterCriteria criteria) {
        return cyclone.getDataLines().stream()
                .filter(dl -> !criteria.hurricaneOnly() || dl.isHurricane())
                .anyMatch(dataLine -> inBoundingBox(dataLine, boundary));
    }

    /**
     * F-REQ-4-b: cyclone has at least one L-marked track point inside the bounding box where wind
     * speed meets the hurricane threshold.
     */
    private boolean hasHurricaneLandfallInBoundary(
            Cyclone cyclone, GeoBoundary boundary, int minWindSpeedKnots) {
        return cyclone.getDataLines().stream()
                .filter(DataLine::isLandfall)
                .filter(dl -> dl.getMaxWindSpeed() >= minWindSpeedKnots)
                .anyMatch(dataLine -> inBoundingBox(dataLine, boundary));
    }

    /**
     * F-REQ-4-c: cyclone has at least one L-marked track point inside Florida's polygon (more
     * accurate than the bounding box which includes open water).
     */
    private boolean hasLandfallInPolygon(Cyclone cyclone, HurricaneFilterCriteria criteria) {
        return cyclone.getDataLines().stream()
                .filter(DataLine::isLandfall)
                .filter(dl -> !criteria.hurricaneOnly() || dl.isHurricane())
                .anyMatch(
                        dataLine ->
                                FloridaPolygon.containsPoint(
                                        toSignedLatitude(dataLine), toSignedLongitude(dataLine)));
    }

    // -------------------------------------------------------------------------
    // Shared coordinate helpers
    // -------------------------------------------------------------------------

    private boolean inBoundingBox(DataLine dl, GeoBoundary boundary) {
        return boundary.containsCoordinate(
                dl.getLatitude(),
                dl.getLatitudeDirection(),
                dl.getLongitude(),
                dl.getLongitudeDirection());
    }

    private double toSignedLatitude(DataLine dl) {
        return dl.getLatitudeDirection() == 'S' ? -dl.getLatitude() : dl.getLatitude();
    }

    private double toSignedLongitude(DataLine dl) {
        return dl.getLongitudeDirection() == 'W' ? -dl.getLongitude() : dl.getLongitude();
    }
}
