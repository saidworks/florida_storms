HURDAT2 Florida Landfall Finder
===============================

This Spring Boot application parses `NOAA HURDAT2 (Best Track)` data to identify hurricanes that'd made landfall in Florida since 1900. The app produces a report with storm name, date of landfall, and maximum wind speed for each event.


### Requirements breakdown:

Functional Requirements:
**HURDAT2 Florida Landfall Finder**

This Spring Boot application parses `NOAA HURDAT2 (Best Track)` data to identify hurricanes that've made landfall in
Florida since 1900. The app produces a report with storm name, date of landfall, and maximum wind speed for each event.

### Requirements breakdown:

**Functional Requirements:**

| Reference | Description                                                                                                                        | Status      |
|-----------|------------------------------------------------------------------------------------------------------------------------------------|-------------|
| F-REQ-1   | Data Parsing: The application must be able to parse the HURDAT2 data set.                                                          | Completed   |
| F-REQ-2   | Landfall Identification: The application must identify hurricanes that have made landfall in Florida since 1900.                   | Review      |
| F-REQ-3   | Report Generation: The application must output a report listing the name, date of landfall, and maximum wind speed for each event. |  Completed |
| F-REQ-4   | Testing plan should be documented outlining functional requirements, with clear strategy                                           | Not started |

**Non-Functional Requirements:**

| Reference   | Description                                                                                                            | Status       |
|-------------|------------------------------------------------------------------------------------------------------------------------|--------------|
| NON-F-REQ-1 | Code Quality: The code should be well-commented and organized to ensure other programmers can understand how it works. | In Progress  |
| NON-F-REQ-2 | Technology Choice: The application should be developed using a programming language of your choice.                    | Completed    |
| NON-F-REQ-3 | Scalability and Maintainability: The application should be designed with scalability and maintainability in mind.      | In Progress  |
| NON-F-REQ-4 | Data Handling Efficiency: The application should be able to handle large data sets efficiently.                        | In Progress  |
| NON-F-REQ-5 | Service Level Agreement with consumer initially is under 10 second to generate report from million records             | Completed |

### Configuration

- cyclone.data.hurdat2 — path or classpath resource to HURDAT2 input.
- Other settings (chunk size, output) can be configured in `application.yml`.


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
