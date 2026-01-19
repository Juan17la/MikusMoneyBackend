package com.example.mikusmoneybackend.auth;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.mikusmoneybackend.config.JwtProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing HTTP cookies, specifically authentication cookies.
 * Uses configuration from JwtProperties for environment-agnostic operation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CookieService {

    private final JwtProperties jwtProperties;

    /**
     * Sets an authentication cookie with the access token.
     */
    public void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = createCookie(jwtProperties.getCookieName(), token);
        cookie.setMaxAge((int) (jwtProperties.getAccessTokenExpiration() / 1000));
        response.addCookie(cookie);
        log.debug("Auth cookie set with expiration: {} seconds", jwtProperties.getAccessTokenExpiration() / 1000);
    }

    /**
     * Sets a refresh token cookie.
     */
    public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = createCookie("REFRESH-TOKEN", refreshToken);
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
        response.addCookie(cookie);
        log.debug("Refresh cookie set with expiration: {} seconds", jwtProperties.getRefreshTokenExpiration() / 1000);
    }

    /**
     * Clears the authentication cookie by setting its max age to 0.
     */
    public void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = createCookie(jwtProperties.getCookieName(), "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.debug("Auth cookie cleared");
    }

    /**
     * Clears the refresh token cookie.
     */
    public void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = createCookie("REFRESH-TOKEN", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.debug("Refresh cookie cleared");
    }

    /**
     * Clears all authentication-related cookies.
     */
    public void clearAllAuthCookies(HttpServletResponse response) {
        clearAuthCookie(response);
        clearRefreshCookie(response);
    }

    /**
     * Extracts the authentication token from request cookies.
     */
    public String getTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, jwtProperties.getCookieName());
    }

    /**
     * Extracts the refresh token from request cookies.
     */
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, "REFRESH-TOKEN");
    }

    /**
     * Gets a cookie value by name.
     */
    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (StringUtils.hasText(value)) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    // Private helper methods

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setPath("/");
        // Note: SameSite is set via response header for older servlet APIs
        return cookie;
    }
}
