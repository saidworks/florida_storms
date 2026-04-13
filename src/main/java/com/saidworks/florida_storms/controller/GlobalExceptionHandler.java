/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.controller;

import com.saidworks.florida_storms.models.exception.BatchProcessingException;
import com.saidworks.florida_storms.models.exception.GeocodingException;
import com.saidworks.florida_storms.models.exception.ReportGenerationException;
import java.time.Instant;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler — maps domain exceptions to safe HTTP responses so
 * stack traces are never leaked to clients.
 */
@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(GeocodingException.class)
    public ResponseEntity<Map<String, Object>> handleGeocoding(GeocodingException ex) {
        log.error("Geocoding error: {}", ex.getMessage(), ex);
        return errorResponse(HttpStatus.BAD_GATEWAY, "Geocoding service unavailable");
    }

    @ExceptionHandler(BatchProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleBatchProcessing(BatchProcessingException ex) {
        log.error("Batch processing error: {}", ex.getMessage(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process cyclone data");
    }

    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleReportGeneration(
            ReportGenerationException ex) {
        log.error("Report generation error: {}", ex.getMessage(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate report");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(
                        Map.of(
                                "status", status.value(),
                                "error", status.getReasonPhrase(),
                                "message", message,
                                "timestamp", Instant.now().toString()));
    }
}
