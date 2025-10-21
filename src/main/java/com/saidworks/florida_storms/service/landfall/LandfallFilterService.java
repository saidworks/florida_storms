/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.landfall;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.models.exception.GeocodingException;
import com.saidworks.florida_storms.service.batch.CycloneProcessingOrchestrator;
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

    private final CycloneProcessingOrchestrator orchestrator;
    private final GeocodingService geocodingService;
    private final ExecutorService serviceTaskExecutor;
    private final ExecutorService ioBlockingTaskExecutor;

    public LandfallFilterService(
            CycloneProcessingOrchestrator orchestrator,
            GeocodingService geocodingService,
            @Qualifier("serviceTaskExecutor") ExecutorService serviceTaskExecutor,
            @Qualifier("ioBlockingTaskExecutor") ExecutorService ioBlockingTaskExecutor) {
        this.orchestrator = orchestrator;
        this.geocodingService = geocodingService;
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

        CompletableFuture<GeoBoundary> boundaryFuture =
                geocodingService.getAreaBoundaries(areaName);

        CompletableFuture<List<Cyclone>> cyclonesFuture =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return orchestrator.processAllCyclones();
                            } catch (Exception e) {
                                log.error("Error loading cyclones", e);
                                throw new GeocodingException("Failed to load cyclones", e);
                            }
                        },
                        ioBlockingTaskExecutor);

        return boundaryFuture.thenCombineAsync(
                cyclonesFuture,
                (boundary, cyclones) -> {
                    log.info("Filtering {} cyclones for area boundaries", cyclones.size());
                    return filterCyclonesByBoundary(cyclones, boundary);
                },
                serviceTaskExecutor);
    }

    /**
     * Filters cyclones by custom latitude/longitude boundaries
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
                    try {
                        List<Cyclone> cyclones = orchestrator.processAllCyclones();
                        return filterCyclonesByBoundary(cyclones, customBoundary);
                    } catch (Exception e) {
                        log.error("Error filtering by custom boundaries", e);
                        throw new GeocodingException("Failed to filter cyclones", e);
                    }
                },
                serviceTaskExecutor);
    }

    /**
     * Core filtering logic - filters cyclones that have landfall points within boundaries
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
     * Checks if a cyclone has any landfall points within the given boundary
     */
    private boolean hasLandfallInBoundary(Cyclone cyclone, GeoBoundary boundary) {
        return cyclone.getDataLines().stream()
                .anyMatch(
                        dataLine ->
                                boundary.containsCoordinate(
                                        dataLine.getLatitude(),
                                        dataLine.getLatitudeDirection(),
                                        dataLine.getLongitude(),
                                        dataLine.getLongitudeDirection()));
    }
}
