# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

HURDAT2 Florida Landfall Finder — Spring Boot app that parses NOAA HURDAT2 hurricane track data, identifies storms that made landfall in Florida (or other Atlantic areas) since 1900, and produces Excel reports. Java 25, Spring Boot 3.5.6, Gradle 9.1.

## Build Commands

```bash
./gradlew build                    # compile + unit tests + spotless check
./gradlew test                     # unit tests only (JUnit 5 + Cucumber)
./gradlew integrationTest          # integration tests (Cucumber BDD, hits Nominatim API)
./gradlew check                    # all checks including integrationTest
./gradlew spotlessApply            # auto-format (google-java-format)
./gradlew bootRun                  # run on port 1234
./gradlew jacocoFullReport         # aggregate coverage → build/reports/jacoco/full/
```

To run a single test class: `./gradlew test --tests 'com.saidworks.florida_storms.service.batch.BatchProcessorServiceTest'`

## Architecture

### Three-phase batch processing pipeline

```
HURDAT2 file → BatchLoaderService → RawBatch[]
									↓
				BatchProcessorService (parallel, async)
									↓
								ProcessedBatch[]
									↓
				BatchMergerService → Cyclone[]
									↓
				LandfallFilterService (boundary + detection strategies)
									↓
				LandfallReportService (Excel via Apache POI)
```

`CycloneProcessingOrchestrator` drives the pipeline. Each phase runs on dedicated thread pools configured in `application.yml` under `executors.*`.

### Port interfaces (hexagonal boundaries)

- `CycloneDataPort` — outbound port for cyclone data loading (implemented by `CycloneProcessingOrchestrator`)
- `GeocodingPort` — outbound port for area-name-to-boundary resolution (implemented by `GeocodingService` via Nominatim API)

Services depend on port interfaces, not concrete implementations.

### Landfall detection strategies (F-REQ-4-a/b/c)

`LandfallFilterService` supports four detection modes via `HurricaneFilterCriteria`:
- **L-marker** (default): only HURDAT2 records flagged with `L` record identifier
- **Geo-coordinate** (F-REQ-4-a): any track point within boundary counts as landfall
- **Hurricane-only** (F-REQ-4-b): only cyclones with wind >= 64 kt at landfall point
- **Florida polygon** (F-REQ-4-c): ray-casting point-in-polygon check via `FloridaPolygon`

The `GET /landfall/advanced` endpoint exposes all four via query params.

### Thread pools

Three `ExecutorService` beans created in `AsyncConfig`, injected via `@Qualifier`:
- `serviceTaskExecutor` — CPU-bound batch processing and filtering
- `controllerTaskExecutor` — controller-level async dispatch
- `ioBlockingTaskExecutor` — file I/O and external API calls

### Test structure

| Source set | Location | Framework | What it tests |
|---|---|---|---|
| `test` | `src/test/java` | JUnit 5 + AssertJ | Unit tests for services |
| `integrationTest` | `src/integrationTest/java` | Cucumber + Spring Boot Test | End-to-end BDD scenarios (requires network for Nominatim) |

Integration tests use `@CucumberContextConfiguration` + `@SpringBootTest` for full Spring context. Feature files live in `src/integrationTest/resources/features/`.

## Key conventions

- **Immutability**: domain value types use Lombok `@Value @Builder` (DataLine, HeaderLine, GeoBoundary, RawBatch). `Cyclone` and `ProcessedBatch` remain mutable (accumulated during batch processing).
- **Records**: `HurricaneFilterCriteria` is a Java record with static factory methods (`defaults()`, `withNoLMarker()`, `withHurricaneOnly()`, `withPolygon()`).
- **Exception handling**: domain exceptions (`GeocodingException`, `BatchProcessingException`, `ReportGenerationException`) are unchecked. `GlobalExceptionHandler` maps them to safe HTTP responses.
- **Formatting**: Spotless with google-java-format runs in CI (Docker build). Run `./gradlew spotlessApply` before committing.
- **Logging**: Log4j2 via Lombok `@Log4j2`. Logback is explicitly excluded.
- **Constructors**: all Spring beans use constructor injection. No field injection with `@Autowired`.

## API endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/cyclones` | All processed cyclones |
| GET | `/landfall/by-area?area=Florida` | Landfalls by area name (L-marker + bounding box) |
| GET | `/landfall/by-coordinates?minLat=&maxLat=&minLon=&maxLon=` | Landfalls by custom boundary |
| GET | `/landfall/advanced?areaName=&useLMarker=&hurricaneOnly=&usePolygon=` | Advanced filter (F-REQ-4-a/b/c) |
| GET | `/landfall/report/excel?areaName=Florida` | Download Excel report |

Swagger UI: `http://localhost:1234/swagger-ui/index.html`
