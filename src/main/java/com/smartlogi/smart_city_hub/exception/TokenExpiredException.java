package com.smartlogi.smart_city_hub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a token (JWT or refresh) has expired.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException() {
        super("Token has expired");
    }

    public TokenExpiredException(String tokenType, String message) {
        super(String.format("%s token expired: %s", tokenType, message));
    }
}
