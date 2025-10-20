/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.exception;

public class GeocodingException extends RuntimeException {

    public GeocodingException(String failureMessage, Throwable cause) {
        super(failureMessage, cause);
    }
}
