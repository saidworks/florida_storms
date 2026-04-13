/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.models.domain.HurricaneFilterCriteria;
import com.saidworks.florida_storms.service.landfall.LandfallFilterService;
import com.saidworks.florida_storms.service.port.GeocodingPort;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FloridaLandfallSteps extends CucumberSpringConfiguration {

    private GeoBoundary areaBoundary;
    private List<Cyclone> result;
    private final LandfallFilterService landfallFilterService;
    private final GeocodingPort geocodingPort;

    public FloridaLandfallSteps(
            LandfallFilterService landfallFilterService, GeocodingPort geocodingPort) {
        this.landfallFilterService = landfallFilterService;
        this.geocodingPort = geocodingPort;
    }

    @Given("The geographic boundaries for Florida")
    public void theGeographicBoundariesForFlorida() {
        // Approximate bounding box for Florida (rough number for testing)
        // Latitude: 24.5°N (Keys) to 31.0°N (Georgia border)
        // Longitude: 87.7°W (near Pensacola) to 80.0°W (Atlantic coast)
        areaBoundary =
                GeoBoundary.builder()
                        .name("Florida")
                        .minLatitude(24.5)
                        .maxLatitude(31.0)
                        .minLongitude(-87.7)
                        .maxLongitude(-80.0)
                        .build();
    }

    @When("We count the cyclones that made landfall within those boundaries")
    public void countCyclonesThatMadeLandfallWithinBoundaries() {
        CompletableFuture<List<Cyclone>> future =
                landfallFilterService.filterByCustomBoundaries(
                        areaBoundary.getMinLatitude(),
                        areaBoundary.getMaxLatitude(),
                        areaBoundary.getMinLongitude(),
                        areaBoundary.getMaxLongitude());
        result = future.join();
    }

    @Then("the total number of landfalls should be {int}")
    public void theTotalNumberOfLandfallsShouldBe(Integer expected) {
        assertThat(result).as("Florida landfall cyclone count").isNotNull();
        assertThat(result.size()).isEqualTo(expected);
    }

    @Given("the {} find the geographic boundaries")
    public void theFindTheGeographicBoundaries(String areaName) {
        CompletableFuture<GeoBoundary> geoBoundaryCompletableFuture =
                geocodingPort.getAreaBoundaries(areaName);
        try {
            areaBoundary = geoBoundaryCompletableFuture.get();

        } catch (InterruptedException | ExecutionException _) {
            Thread.currentThread().interrupt();
        }
    }

    // NO SONAR
    @DataTableType
    public GeoBoundary geoBoundaryTransformer(Map<String, String> row) {
        return new GeoBoundary(
                row.get("name"),
                Double.parseDouble(row.get("minLatitude")),
                Double.parseDouble(row.get("maxLatitude")),
                Double.parseDouble(row.get("minLongitude")),
                Double.parseDouble(row.get("maxLongitude")));
    }

    @And("^Geographic boundaries should be within the following geoboundary$")
    public void geographicBoundariesShouldMatch(GeoBoundary geoBoundary) {
        assertThat(areaBoundary)
                .matches(
                        f ->
                                f.getMinLatitude() >= geoBoundary.getMinLatitude()
                                        && f.getMaxLatitude() <= geoBoundary.getMaxLatitude()
                                        && f.getMaxLongitude() <= geoBoundary.getMaxLongitude());
    }

    // -------------------------------------------------------------------------
    // F-REQ-4-a: geo-coordinate detection (no L marker required)
    // -------------------------------------------------------------------------

    @When("We count cyclones using geo-coordinate detection within those boundaries")
    public void countCyclonesUsingGeoCoordinateDetection() {
        result =
                landfallFilterService
                        .filterByAreaLandfallAdvanced(
                                areaBoundary.getName(), HurricaneFilterCriteria.withNoLMarker())
                        .join();
    }

    @Then("the geo-coordinate landfall count should be at least {int}")
    public void geoCoordinateLandfallCountShouldBeAtLeast(int minExpected) {
        assertThat(result).as("Geo-coordinate Florida landfall count").isNotNull();
        assertThat(result.size())
                .as("Geo-coordinate count should be >= %d", minExpected)
                .isGreaterThanOrEqualTo(minExpected);
    }

    // -------------------------------------------------------------------------
    // F-REQ-4-b: hurricane-strength filter
    // -------------------------------------------------------------------------

    @When("We count hurricane-strength cyclones that made landfall within those boundaries")
    public void countHurricaneStrengthCyclones() {
        result =
                landfallFilterService
                        .filterByAreaLandfallAdvanced(
                                areaBoundary.getName(), HurricaneFilterCriteria.withHurricaneOnly())
                        .join();
    }

    @Then("the hurricane landfall count should be greater than {int}")
    public void hurricaneLandfallCountShouldBeGreaterThan(int minExpected) {
        assertThat(result).as("Hurricane Florida landfall count").isNotNull();
        assertThat(result.size())
                .as("Hurricane count should be > %d", minExpected)
                .isGreaterThan(minExpected);
    }

    @Then("the hurricane landfall count should be less than {int}")
    public void hurricaneLandfallCountShouldBeLessThan(int maxExpected) {
        assertThat(result).as("Hurricane Florida landfall count").isNotNull();
        assertThat(result.size())
                .as("Hurricane count should be < %d (subset of all landfalls)", maxExpected)
                .isLessThan(maxExpected);
    }
}
