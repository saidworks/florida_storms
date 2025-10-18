HURDAT2 Florida Landfall Finder
===============================

This Spring Boot application parses `NOAA HURDAT2 (Best Track)` data to identify hurricanes that'd made landfall in Florida since 1900. The app produces a report with storm name, date of landfall, and maximum wind speed for each event.

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
