/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.controller;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.exception.BatchProcessingException;
import com.saidworks.florida_storms.service.CycloneProcessingOrchestrator;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cyclones")
public class CycloneController {
    private final CycloneProcessingOrchestrator orchestrator;
    private final ExecutorService controllerTaskExecutor;

    public CycloneController(
            CycloneProcessingOrchestrator orchestrator, ExecutorService controllerTaskExecutor) {
        this.orchestrator = orchestrator;
        this.controllerTaskExecutor = controllerTaskExecutor;
    }

    @GetMapping
    public List<Cyclone> getAllCyclones() {
        return CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return orchestrator.processAllCyclones();
                            } catch (IOException e) {
                                throw new BatchProcessingException(
                                        "controller failed to process batch", e);
                            }
                        },
                        controllerTaskExecutor)
                .join();
    }
}
