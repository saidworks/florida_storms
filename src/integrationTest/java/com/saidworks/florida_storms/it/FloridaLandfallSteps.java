/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.service.landfall.GeocodingService;
import com.saidworks.florida_storms.service.landfall.LandfallFilterService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.NotImplementedException;

public class FloridaLandfallSteps extends CucumberSpringConfiguration {

    private GeoBoundary floridaBoundary;
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
        floridaBoundary =
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
                        floridaBoundary.getMinLatitude(),
                        floridaBoundary.getMaxLatitude(),
                        floridaBoundary.getMinLongitude(),
                        floridaBoundary.getMaxLongitude());
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
            floridaBoundary = geoBoundaryCompletableFuture.get();
            assertThat(floridaBoundary)
                    .matches(
                            f ->
                                    f.getMinLatitude() >= 24.396308
                                            && f.getMaxLatitude() <= 31.000762
                                            && f.getMaxLongitude() <= -79.974306);

        } catch (InterruptedException | ExecutionException _) {
            Thread.currentThread().interrupt();
        }
    }

    @And("Geographic boundaries should match")
    public void geographicBoundarisShouldMatch() {
        throw new NotImplementedException("test method not implemented yet");
    }
}
