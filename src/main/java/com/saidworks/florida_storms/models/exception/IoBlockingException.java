/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.exception;

/**
 * Exception representing IO blocking related failures in the Florida Storms application.
 */
public class IoBlockingException extends RuntimeException {

    public IoBlockingException(String failureMessage, Throwable cause) {
        super(failureMessage, cause);
    }
}
