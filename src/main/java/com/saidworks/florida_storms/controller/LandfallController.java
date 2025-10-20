/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.controller;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.service.landfall.LandfallFilterService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for filtering storms by landfall location
 */
@RestController
@RequestMapping("/landfall")
@Log4j2
public class LandfallController {

    private final LandfallFilterService landfallFilterService;
    private final ExecutorService controllerTaskExecutor;

    public LandfallController(
            LandfallFilterService landfallFilterService,
            @Qualifier("controllerTaskExecutor") ExecutorService controllerTaskExecutor) {
        this.landfallFilterService = landfallFilterService;
        this.controllerTaskExecutor = controllerTaskExecutor;
    }

    /**
     * Get all storms that made landfall in a specific area
     * Example: GET /landfall/by-area?area=Miami
     */
    @GetMapping("/by-area")
    public CompletableFuture<List<Cyclone>> getStormsByArea(
            @RequestParam(value = "area", defaultValue = "Florida") String areaName) {

        log.info("Request received: filtering storms by area: {}", areaName);

        return CompletableFuture.runAsync(
                        () -> log.info("Processing request for area: {}", areaName),
                        controllerTaskExecutor)
                .thenCompose(_ -> landfallFilterService.filterByAreaLandfall(areaName));
    }

    /**
     * Get storms by custom latitude/longitude boundaries
     * Example: GET /landfall/by-coordinates?minLat=24.0&maxLat=31.0&minLon=-87.0&maxLon=-80.0
     */
    @GetMapping("/by-coordinates")
    public CompletableFuture<List<Cyclone>> getStormsByCoordinates(
            @RequestParam("minLat") double minLat,
            @RequestParam("maxLat") double maxLat,
            @RequestParam("minLon") double minLon,
            @RequestParam("maxLon") double maxLon) {

        log.info("Request received: filtering storms by coordinates");

        return CompletableFuture.runAsync(
                        () ->
                                log.info(
                                        "Processing custom boundary request: lat[{},{}],"
                                                + " lon[{},{}]",
                                        minLat,
                                        maxLat,
                                        minLon,
                                        maxLon),
                        controllerTaskExecutor)
                .thenCompose(
                        v ->
                                landfallFilterService.filterByCustomBoundaries(
                                        minLat, maxLat, minLon, maxLon));
    }
}
