/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.port;

import com.saidworks.florida_storms.models.domain.Cyclone;
import java.io.IOException;
import java.util.List;

/**
 * Outbound port for loading and processing cyclone track data.
 *
 * <p>Decouples consumers (controllers, filter services) from the concrete batch-processing
 * pipeline so implementations can be swapped (e.g., in-memory stub for unit tests).
 */
public interface CycloneDataPort {
    List<Cyclone> processAllCyclones() throws IOException;
}
