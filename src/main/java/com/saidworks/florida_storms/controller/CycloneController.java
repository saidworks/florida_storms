/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.controller;

import com.saidworks.florida_storms.models.Cyclone;
import com.saidworks.florida_storms.service.io.CycloneProcessingOrchestrator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cyclones")
public class CycloneController {
    private final CycloneProcessingOrchestrator orchestrator;

    public CycloneController(CycloneProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping
    public List<Cyclone> getAllCyclones() throws Exception {
        return orchestrator.processAllCyclones();
    }
}
