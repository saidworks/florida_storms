/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.landfall;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saidworks.florida_storms.client.NominatimClient;
import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.models.exception.GeocodingException;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Service to fetch geographic boundaries using Nominatim OpenStreetMap API
 */
@Service
@Log4j2
public class GeocodingService {

    private final NominatimClient nominatimClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService serviceTaskExecutor;

    public GeocodingService(ExecutorService serviceTaskExecutor, NominatimClient nominatimClient) {
        this.serviceTaskExecutor = serviceTaskExecutor;
        this.nominatimClient = nominatimClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches boundaries for a custom area/location
     */
    public CompletableFuture<GeoBoundary> getAreaBoundaries(String areaName) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        log.info("Fetching boundaries for area: {}", areaName);

                        HttpResponse<String> response = nominatimClient.search(areaName);

                        if (response.statusCode() != 200) {
                            throw new GeocodingException(
                                    "Failed to fetch boundaries: HTTP " + response.statusCode(),
                                    new RuntimeException());
                        }

                        JsonNode jsonArray = objectMapper.readTree(response.body());
                        if (jsonArray.isEmpty()) {
                            throw new GeocodingException(
                                    "No results found for area: " + areaName,
                                    new RuntimeException());
                        }

                        JsonNode firstResult = jsonArray.get(0);
                        JsonNode boundingBox = firstResult.get("boundingbox");

                        if (boundingBox == null || boundingBox.size() < 4) {
                            throw new GeocodingException(
                                    "Invalid bounding box data", new RuntimeException());
                        }

                        return GeoBoundary.builder()
                                .name(areaName)
                                .minLatitude(boundingBox.get(0).asDouble())
                                .maxLatitude(boundingBox.get(1).asDouble())
                                .minLongitude(boundingBox.get(2).asDouble())
                                .maxLongitude(boundingBox.get(3).asDouble())
                                .build();
                    } catch (Exception e) {
                        log.error("Error fetching boundaries for area: {}", areaName, e);
                        Thread.currentThread().interrupt();
                        throw new GeocodingException("Failed to fetch geographic boundaries ", e);
                    }
                },
                serviceTaskExecutor);
    }
}
