Feature: Florida cyclone landfall count
  As a user of the hurricane landfall system
  I want to verify the number of cyclones that made landfall in Florida
  So that I can trust the integration between parsing and filtering

  Scenario: Verify Florida landfall count
    Given The geographic boundaries for Florida
    When We count the cyclones that made landfall within those boundaries
    Then the total number of landfalls should be 167

  Scenario Outline: Verify landfall count <areaName>
    Given the <areaName> find the geographic boundaries
    And Geographic boundaries should be within the following geoboundary
      | name       | minLatitude   | maxLatitude   | minLongitude   | maxLongitude   |
      | <areaName> | <minLatitude> | <maxLatitude> | <minLongitude> | <maxLongitude> |
    When We count the cyclones that made landfall within those boundaries
    Then the total number of landfalls should be <count>

    Examples:
      | areaName | count | minLatitude | maxLatitude | minLongitude | maxLongitude |
      | Florida  | 167   | 24.396308   | 31.000762   | -87.634896   | -79.974306   |
      | Texas    | 75    | 25.83706    | 36.5004529  | -106.6458459 | -93.5078217  |
