package com.example.mikusmoneybackend.config.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public static ResourceNotFoundException account() {
        return new ResourceNotFoundException("Account not found");
    }

    public static ResourceNotFoundException user() {
        return new ResourceNotFoundException("User not found");
    }

    public static ResourceNotFoundException credentials() {
        return new ResourceNotFoundException("Credentials not found");
    }

    public static ResourceNotFoundException receiverAccount() {
        return new ResourceNotFoundException("Receiver account not found");
    }

    public static ResourceNotFoundException savingsPig() {
        return new ResourceNotFoundException("Savings pig not found");
    }

    public static ResourceNotFoundException byId(String resource, Long id) {
        return new ResourceNotFoundException(resource + " with ID " + id + " not found");
    }
}
