/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.service.report;

import com.saidworks.florida_storms.models.domain.Cyclone;
import com.saidworks.florida_storms.models.domain.DataLine;
import com.saidworks.florida_storms.models.exception.GeocodingException;
import com.saidworks.florida_storms.service.landfall.LandfallFilterService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class LandfallReportService {

    private final LandfallFilterService landfallFilterService;
    private final ExecutorService serviceTaskExecutor;

    public LandfallReportService(
            LandfallFilterService landfallFilterService,
            @Qualifier("serviceTaskExecutor") ExecutorService serviceTaskExecutor) {
        this.landfallFilterService = landfallFilterService;
        this.serviceTaskExecutor = serviceTaskExecutor;
    }

    /**
     * Generates an Excel report for landfall events in a specific area
     */
    public CompletableFuture<byte[]> generateLandfallReportByArea(String areaName) {
        log.info("Generating landfall report for area: {}", areaName);

        return landfallFilterService
                .filterByAreaLandfall(areaName)
                .thenApplyAsync(this::createExcelReport, serviceTaskExecutor);
    }

    /**
     * Creates Excel workbook from filtered cyclones
     */
    private byte[] createExcelReport(List<Cyclone> cyclones) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Landfall Events");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(headerRow, 0, "Storm Name", headerStyle);
            createHeaderCell(headerRow, 1, "Date of Landfall", headerStyle);
            createHeaderCell(headerRow, 2, "Max Wind Speed (mph)", headerStyle);
            createHeaderCell(headerRow, 3, "Latitude", headerStyle);
            createHeaderCell(headerRow, 4, "Longitude", headerStyle);

            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // Process each cyclone and its landfall events
            for (Cyclone cyclone : cyclones) {
                String stormName = cyclone.getHeader().getName();

                // Get all landfall data lines for this cyclone
                List<DataLine> landfallEvents =
                        cyclone.getDataLines().stream().filter(DataLine::isLandfall).toList();

                for (DataLine landfall : landfallEvents) {
                    Row row = sheet.createRow(rowNum++);

                    // Storm Name
                    row.createCell(0).setCellValue(stormName);

                    // Date of Landfall
                    Cell dateCell = row.createCell(1);
                    dateCell.setCellValue(landfall.getDateTime().format(dateFormatter));
                    dateCell.setCellStyle(dateStyle);

                    // Max Wind Speed
                    if (landfall.getMaxWindSpeed() > 0) {
                        row.createCell(2).setCellValue(landfall.getMaxWindSpeed());
                    } else {
                        row.createCell(2).setCellValue("N/A");
                    }

                    // Latitude
                    row.createCell(3)
                            .setCellValue(
                                    landfall.getLatitude() + "" + landfall.getLatitudeDirection());

                    // Longitude
                    row.createCell(4)
                            .setCellValue(
                                    landfall.getLongitude()
                                            + ""
                                            + landfall.getLongitudeDirection());
                }
            }

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("Generated Excel report with {} landfall events", rowNum - 1);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error creating Excel report", e);
            throw new GeocodingException("Failed to create Excel report", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy-mm-dd hh:mm"));
        return style;
    }

    private void createHeaderCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
