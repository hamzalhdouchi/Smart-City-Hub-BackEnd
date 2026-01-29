package com.smartlogi.smart_city_hub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invalid business operation is attempted.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String operation, String reason) {
        super(String.format("Cannot perform '%s': %s", operation, reason));
    }
}
