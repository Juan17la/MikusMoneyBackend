package com.example.mikusmoneybackend.config.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a duplicate transaction is detected via idempotency key.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateTransactionException extends RuntimeException {
    
    public DuplicateTransactionException(String message) {
        super(message);
    }

    public DuplicateTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateTransactionException duplicateKey() {
        return new DuplicateTransactionException("Transaction already processed with this idempotency key");
    }
}
