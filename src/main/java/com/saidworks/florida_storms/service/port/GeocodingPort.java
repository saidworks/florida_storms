/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.port;

import com.saidworks.florida_storms.models.domain.GeoBoundary;
import java.util.concurrent.CompletableFuture;

/**
 * Outbound port for resolving a geographic area name to its bounding coordinates.
 *
 * <p>Decouples consumers from the concrete geocoding provider (currently Nominatim) so
 * implementations can be swapped or stubbed in tests.
 */
public interface GeocodingPort {
    CompletableFuture<GeoBoundary> getAreaBoundaries(String areaName);
}
