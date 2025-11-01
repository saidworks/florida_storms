Feature: Florida cyclone landfall count
  As a user of the hurricane landfall system
  I want to verify the number of cyclones that made landfall in Florida
  So that I can trust the integration between parsing and filtering

  Scenario: Verify Florida landfall count
    Given the geographic boundaries for Florida
    When I count the cyclones that made landfall within those boundaries
    Then the total number of landfalls should be 167
