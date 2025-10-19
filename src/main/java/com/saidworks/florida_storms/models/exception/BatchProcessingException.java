/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.exception;

/**
 * Exception thrown when batch processing operations fail.
 */
public class BatchProcessingException extends RuntimeException {

    public BatchProcessingException(String failureMessage, Throwable cause) {
        super(failureMessage, cause);
    }
}
