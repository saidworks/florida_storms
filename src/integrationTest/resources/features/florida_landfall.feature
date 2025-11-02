Feature: Florida cyclone landfall count
  As a user of the hurricane landfall system
  I want to verify the number of cyclones that made landfall in Florida
  So that I can trust the integration between parsing and filtering

  Scenario: Verify Florida landfall count
    Given The geographic boundaries for Florida
    When We count the cyclones that made landfall within those boundaries
    Then the total number of landfalls should be 167

  @ignore
  Scenario Outline: Verify landfall count <areaName>
    Given the <areaName> find the geographic boundaries
    And Geographic boundaries should match
    When We count the cyclones that made landfall within those boundaries
    Then the total number of landfalls should be <count>

    Examples:
      | areaName | count |
      | Florida  | 167   |
      | Texas    | 50    |
