Feature: Florida cyclone landfall count
  As a user of the hurricane landfall system
  I want to verify the number of cyclones that made landfall in given area, either by:
    - Geo-Coordinates
    - Area name
  So that I can trust the integration between parsing, batching and filtering

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

  # F-REQ-4-a: landfall detection without the HURDAT2 L record identifier.
  # Geo-coordinate mode tracks every storm point within the boundary, so it should find
  # at least as many cyclones as the L-marker mode (>= 167).
  Scenario: Verify Florida landfall using geo-coordinate detection (F-REQ-4-a)
    Given The geographic boundaries for Florida
    When We count cyclones using geo-coordinate detection within those boundaries
    Then the geo-coordinate landfall count should be at least 167

  # F-REQ-4-b: filter by hurricane strength (wind speed >= 64 kt).
  # Only a subset of all landfalls are full hurricanes, so the count must be > 0 and < 167.
  Scenario: Verify Florida hurricane-strength landfall count (F-REQ-4-b)
    Given The geographic boundaries for Florida
    When We count hurricane-strength cyclones that made landfall within those boundaries
    Then the hurricane landfall count should be greater than 0
    And the hurricane landfall count should be less than 167
