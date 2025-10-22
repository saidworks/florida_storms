/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.controller;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.service.landfall.LandfallFilterService;
import com.saidworks.florida_storms.service.report.LandfallReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for filtering storms by landfall location
 */
@Tag(
        name = "Landfall",
        description =
                "Operations for filtering storms by landfall locations and generating reports")
@RestController
@RequestMapping("/landfall")
@Log4j2
public class LandfallController {

    private final LandfallFilterService landfallFilterService;
    private final LandfallReportService landfallReportService;
    private final ExecutorService controllerTaskExecutor;

    public LandfallController(
            LandfallFilterService landfallFilterService,
            LandfallReportService landfallReportService,
            @Qualifier("controllerTaskExecutor") ExecutorService controllerTaskExecutor) {
        this.landfallFilterService = landfallFilterService;
        this.landfallReportService = landfallReportService;
        this.controllerTaskExecutor = controllerTaskExecutor;
    }

    /**
     * Get all storms that made landfall in a specific area
     * Example: GET /landfall/by-area?area=Miami
     */
    @Operation(
            summary = "Filter storms by area",
            description =
                    "Retrieve all storms that made landfall in a specific geographic area. "
                            + "The search is performed asynchronously for better performance.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved storms for the specified area",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Cyclone.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid area parameter",
                        content = @Content)
            })
    @GetMapping("/by-area")
    public CompletableFuture<List<Cyclone>> getStormsByArea(
            @Parameter(
                            description = "Name of the geographic area to filter storms by",
                            example = "Miami")
                    @RequestParam(value = "area", defaultValue = "Florida")
                    String areaName) {

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
    @Operation(
            summary = "Filter storms by coordinates",
            description =
                    "Retrieve storms that made landfall within custom latitude and longitude"
                            + " boundaries. Useful for defining precise geographic regions.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description =
                                "Successfully retrieved storms within the specified coordinates",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Cyclone.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid coordinate parameters",
                        content = @Content)
            })
    @GetMapping("/by-coordinates")
    public CompletableFuture<List<Cyclone>> getStormsByCoordinates(
            @Parameter(description = "Minimum latitude", example = "24.0", required = true)
                    @RequestParam("minLat")
                    double minLat,
            @Parameter(description = "Maximum latitude", example = "31.0", required = true)
                    @RequestParam("maxLat")
                    double maxLat,
            @Parameter(description = "Minimum longitude", example = "-87.0", required = true)
                    @RequestParam("minLon")
                    double minLon,
            @Parameter(description = "Maximum longitude", example = "-80.0", required = true)
                    @RequestParam("maxLon")
                    double maxLon) {

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
                        _ ->
                                landfallFilterService.filterByCustomBoundaries(
                                        minLat, maxLat, minLon, maxLon));
    }

    @Operation(
            summary = "Download landfall report",
            description =
                    "Generate and download an Excel report containing detailed landfall data for a"
                        + " specific area. The report includes comprehensive storm information and"
                        + " statistics.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully generated and returned Excel report",
                        content =
                                @Content(
                                        mediaType =
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid area parameter",
                        content = @Content),
                @ApiResponse(
                        responseCode = "500",
                        description = "Error generating report",
                        content = @Content)
            })
    @GetMapping("/report/excel")
    public CompletableFuture<ResponseEntity<byte[]>> downloadLandfallReport(
            @Parameter(
                            description = "Name of the area to generate the report for",
                            example = "Florida",
                            required = true)
                    @RequestParam
                    String areaName) {

        return landfallReportService
                .generateLandfallReportByArea(areaName)
                .thenApply(
                        excelData -> {
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                            headers.setContentDisposition(
                                    ContentDisposition.attachment()
                                            .filename(
                                                    "landfall_report_"
                                                            + areaName.replaceAll("\\s+", "_")
                                                            + ".xlsx")
                                            .build());

                            return ResponseEntity.ok().headers(headers).body(excelData);
                        });
    }
}
