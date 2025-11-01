/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.it;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.domain.GeoBoundary;
import com.saidworks.florida_storms.service.landfall.LandfallFilterService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.assertj.core.api.Assertions;

public class FloridaLandfallSteps extends CucumberSpringConfiguration {

    private GeoBoundary floridaBoundary;
    private List<Cyclone> result;
    private final LandfallFilterService landfallFilterService;

    public FloridaLandfallSteps(LandfallFilterService landfallFilterService) {
        this.landfallFilterService = landfallFilterService;
    }

    @Given("the geographic boundaries for Florida")
    public void theGeographicBoundariesForFlorida() {
        // Approximate bounding box for Florida
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

    @When("I count the cyclones that made landfall within those boundaries")
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
        Assertions.assertThat(result).as("Florida landfall cyclone count").isNotNull();
        Assertions.assertThat(result.size()).isEqualTo(expected);
    }
}
