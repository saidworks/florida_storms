package com.saidworks.florida_storms.controller;

import com.saidworks.florida_storms.models.Cyclone;
import com.saidworks.florida_storms.service.io.CycloneProcessingOrchestrator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
