package com.example.mikusmoneybackend.config.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base exception class for all API exceptions.
 * Provides a consistent structure for error responses.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected ApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected ApiException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
