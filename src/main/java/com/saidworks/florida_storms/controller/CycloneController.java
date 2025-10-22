/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.controller;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.exception.BatchProcessingException;
import com.saidworks.florida_storms.service.batch.CycloneProcessingOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cyclones", description = "Operations related to cyclone data processing and retrieval")
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

    @Operation(
            summary = "Retrieve all cyclones",
            description =
                    "Fetches and processes all available cyclone data from the data source. This"
                            + " operation performs batch processing and may take some time to"
                            + " complete.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved all cyclones",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Cyclone.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal server error during batch processing",
                        content = @Content)
            })
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
