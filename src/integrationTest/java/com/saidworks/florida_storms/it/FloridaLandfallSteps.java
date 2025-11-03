/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.service.landfall.GeocodingService;
import com.saidworks.florida_storms.service.landfall.LandfallFilterService;
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
    private final GeocodingService geocodingService;

    public FloridaLandfallSteps(
            LandfallFilterService landfallFilterService, GeocodingService geocodingService) {
        this.landfallFilterService = landfallFilterService;
        this.geocodingService = geocodingService;
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
                geocodingService.getAreaBoundaries(areaName);
        try {
            areaBoundary = geoBoundaryCompletableFuture.get();

        } catch (InterruptedException | ExecutionException _) {
            Thread.currentThread().interrupt();
        }
    }
    //NOSONAR
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
    public void geographicBoundarisShouldMatch(GeoBoundary geoBoundary) {
        assertThat(areaBoundary)
                .matches(
                        f ->
                                f.getMinLatitude() >= geoBoundary.getMinLatitude()
                                        && f.getMaxLatitude() <= geoBoundary.getMaxLatitude()
                                        && f.getMaxLongitude() <= geoBoundary.getMaxLongitude());
    }
}
