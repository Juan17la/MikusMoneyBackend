package com.example.mikusmoneybackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Configuration properties for JWT token management.
 * Values are loaded from application.properties with prefix 'jwt'.
 * 
 * Example configuration:
 * jwt.secret=your-base64-encoded-secret-key
 * jwt.access-token-expiration=86400000
 * jwt.refresh-token-expiration=604800000
 * jwt.cookie-name=AUTH-TOKEN
 */
@Data
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Base64-encoded secret key for signing JWT tokens.
     * Must be at least 256 bits (32 bytes) for HS256 algorithm.
     */
    @NotBlank(message = "JWT secret key must be configured")
    private String secret;

    /**
     * Access token expiration time in milliseconds.
     * Default: 24 hours (86400000ms)
     */
    @Min(value = 60000, message = "Access token expiration must be at least 1 minute")
    private long accessTokenExpiration = 86400000L;

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days (604800000ms)
     */
    @Min(value = 60000, message = "Refresh token expiration must be at least 1 minute")
    private long refreshTokenExpiration = 604800000L;

    /**
     * Cookie name for storing the auth token.
     */
    private String cookieName = "AUTH-TOKEN";

    /**
     * Whether cookies should be marked as secure (HTTPS only).
     * Should be true in production.
     */
    private boolean cookieSecure = false;

    /**
     * SameSite attribute for cookies.
     * Options: Strict, Lax, None
     */
    private String cookieSameSite = "Lax";
}
