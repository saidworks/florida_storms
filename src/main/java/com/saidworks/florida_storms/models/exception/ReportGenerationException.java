/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.exception;

/**
 * Thrown when report generation (e.g. Excel export) fails.
 */
public class ReportGenerationException extends RuntimeException {

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
