HURDAT2 Florida Landfall Finder
===============================

This Spring Boot application parses `NOAA HURDAT2 (Best Track)` data to identify hurricanes that'd made landfall in Florida (this is flexible you can search for other areas in Atlantic region as well refer to [Generating Excel Reports](#Generating-Excel-Reports))
since 1900.

The app produces a report with storm name, date of landfall, and maximum wind speed for each event.


### Requirements breakdown:

**Functional Requirements:**

| Reference | Description                                                                                                                        | Status    |
|-----------|------------------------------------------------------------------------------------------------------------------------------------|-----------|
| F-REQ-1   | Data Parsing: The application must be able to parse the HURDAT2 data set.                                                          | Completed |
| F-REQ-2   | Landfall Identification: The application must identify hurricanes that have made landfall in Florida since 1900.                   | Completed |
| F-REQ-3   | Report Generation: The application must output a report listing the name, date of landfall, and maximum wind speed for each event. | Completed |
| F-REQ-4   | Landfall Identification: Create a feature to not use `L` for landfall identification.                                              | QA        |
| F-REQ-5   | Add client to get latitude and longtitude of an area by name                                                                       | Completed |
| F-REQ-6   | Filter hurricanes based latitude and longitude                                                                                     | Completed |

**Non-Functional Requirements:**

| Reference   | Description                                                                                                            | Status      |
|-------------|------------------------------------------------------------------------------------------------------------------------|-------------|
| NON-F-REQ-1 | Code Quality: The code should be well-commented and organized to ensure other programmers can understand how it works. | Completed   |
| NON-F-REQ-2 | Technology Choice: The application should be developed using a programming language of your choice.                    | Completed   |
| NON-F-REQ-3 | Scalability and Maintainability: The application should be designed with scalability and maintainability in mind.      | QA          |
| NON-F-REQ-4 | Data Handling Efficiency: The application should be able to handle large data sets efficiently.                        | In Progress |
| NON-F-REQ-5 | Service Level Agreement with consumer initially is under 10 second to generate report from million records             | Completed   |
| NON-F-REQ-6 | Add spring open api swagger docs for endpoints for readiblity by consumers | Completed   |
| NON-F-REQ-7 | Testing plan should be documented outlining functional requirements, with clear strategy                                           | Not started |

### Technology Stack:
The stack used in this project consists of the following:
- Java 25 (Latest Long Term Supported Version)
- you can find the latest JDK Temurin 25 here: https://adoptium.net/temurin/releases/
- Spring Boot 3.5.6
- dependency for spring is managed through spring boot dependency management plugin this make patching easier for regular security updates
- Gradle 9.1
- Spotless Plugin for code formatting

### Running with Docker (host network)
- If you want the container to share the host's network namespace (so the app is reachable at http://localhost:1234 on the host), you can run Docker with host networking.
- Important: --network host is supported on Linux Docker Engine only. It does not behave the same (and is not supported) on Docker Desktop for macOS/Windows.

Steps:
1. Build the image:
```bash
		docker build -t hurdat2-florida .
```
2.a  Run using host network (Linux only — no -p needed):
```bash
	docker run --rm --network host -e SPRING_PROFILES_ACTIVE=dev hurdat2-florida
```

2.b Alternative for macOS/Windows (use port mapping):
```bash
	docker run --rm -p 1234:1234 -e SPRING_PROFILES_ACTIVE=dev hurdat2-florida
```


### Configuration

- cyclone.data.hurdat2 — path or classpath resource to HURDAT2 input.
- Other settings (chunk size, output) can be configured in `application.yml`.

### API Documentation

Explore and test the API using Swagger UI. In a development environment, the interactive documentation is available at: [http://localhost:1234/swagger-ui/index.html](http://localhost:1234/swagger-ui/index.html). This provides a convenient way to understand and interact with all available endpoints and their parameters.



### Generating Excel Reports

After the application starts, you can generate landfall reports by calling the REST endpoints:

**Option 1: Browser (Easiest)**
1. Start the application: `./gradlew bootRun`
2. Open your web browser and navigate to:
```
http://localhost:1234/landfall/report/excel?areaName=Florida
```
3. The Excel file will automatically download to your Downloads folder
4. Open the downloaded file with Excel, LibreOffice, or Google Sheets

**Other browser examples:**
- Gulf Coast: `http://localhost:1234/landfall/report/excel?areaName=Gulf%20Coast`
- Miami area: `http://localhost:1234/landfall/report/excel?areaName=Miami`
- Custom area: `http://localhost:1234/landfall/report/excel?areaName=YourAreaName`

**Option 2: Command Line (curl)**
```bash
curl localhost:1234/landfall/report/excel?areaName=Florida --output report.xls
```
Notes
-----
Designed for easy local use and scalable to larger workloads; tune thread and memory settings for large files. Use the Gradle wrapper to ensure consistent builds.

Contributing
------------
Open-source and welcoming to contributions — file issues, suggest improvements, or submit pull requests. Community feedback is encouraged.
