package com.example.mikusmoneybackend.config.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid credentials");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Invalid or expired token");
    }

    public static AuthenticationException notAuthenticated() {
        return new AuthenticationException("User is not authenticated");
    }

    public static AuthenticationException invalidPin() {
        return new AuthenticationException("Invalid PIN code");
    }

    public static AuthenticationException invalidPassword() {
        return new AuthenticationException("Invalid password");
    }

    public static AuthenticationException accountLocked() {
        return new AuthenticationException("Account is locked");
    }
}
