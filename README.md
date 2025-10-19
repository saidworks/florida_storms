HURDAT2 Florida Landfall Finder
===============================

This Spring Boot application parses `NOAA HURDAT2 (Best Track)` data to identify hurricanes that'd made landfall in Florida since 1900. The app produces a report with storm name, date of landfall, and maximum wind speed for each event.


### Requirements breakdown:

Functional Requirements:

| Reference | Description                              | Status      |
|-----------|------------------------------------------|-------------|
| F-REQ-1  | Data Parsing: The application must be able to parse the HURDAT2 data set. | In Progress |
| F-REQ-2  | Landfall Identification: The application must identify hurricanes that have made landfall in Florida since 1900. | Not Started |
| F-REQ-3  | Report Generation: The application must output a report listing the name, date of landfall, and maximum wind speed for each event. | Not Started |

Non-Functional Requirements:

| Reference | Description                              | Status      |
|-----------|------------------------------------------|-------------|
| NON-F-REQ-1 | Code Quality: The code should be well-commented and organized to ensure other programmers can understand how it works. | In Progress |
| NON-F-REQ-2 | Technology Choice: The application should be developed using a programming language of your choice. | Completed |
| NON-F-REQ-3 | Scalability and Maintainability: The application should be designed with scalability and maintainability in mind. | In Progress |
| NON-F-REQ-4 | Data Handling Efficiency: The application should be able to handle large data sets efficiently. | Not Started |


Quick start (Gradle)
--------------------
1. Put the HURDAT2 file on disk or in resources and set `cyclone.data.hurdat2` in `src/main/resources/application.yml` or via environment variables.
2. Run with the Gradle wrapper:
   - Start in development: `./gradlew bootRun`
   - Build and run artifact: `./gradlew clean build && java -jar build/libs/<your-artifact>.jar`
3. Check console or configured output location for the report.

Configuration
-------------
- cyclone.data.hurdat2 — path or classpath resource to HURDAT2 input.
- Other settings (chunk size, output) can be configured in `application.yml`.

Notes
-----
Designed for easy local use and scalable to larger workloads; tune thread and memory settings for large files. Use the Gradle wrapper to ensure consistent builds.

Contributing
------------
Open-source and welcoming to contributions — file issues, suggest improvements, or submit pull requests. Community feedback is encouraged.
