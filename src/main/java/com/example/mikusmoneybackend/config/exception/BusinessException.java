package com.example.mikusmoneybackend.config.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for business rule violations.
 */
public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR");
    }

    public BusinessException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public static BusinessException emailAlreadyExists() {
        return new BusinessException("Email already exists", "EMAIL_EXISTS");
    }

    public static BusinessException phoneAlreadyExists() {
        return new BusinessException("Phone number already exists", "PHONE_EXISTS");
    }

    public static BusinessException passwordMismatch() {
        return new BusinessException("Passwords do not match", "PASSWORD_MISMATCH");
    }

    public static BusinessException pinMismatch() {
        return new BusinessException("PIN codes do not match", "PIN_MISMATCH");
    }

    public static BusinessException userNotAdult() {
        return new BusinessException("User must be at least 18 years old to register", "USER_NOT_ADULT");
    }

    public static BusinessException insufficientFunds() {
        return new BusinessException("Insufficient funds", "INSUFFICIENT_FUNDS");
    }

    public static BusinessException invalidAmount() {
        return new BusinessException("Amount must be greater than zero", "INVALID_AMOUNT");
    }
}
